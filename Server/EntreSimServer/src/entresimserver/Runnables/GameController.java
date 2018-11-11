/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables;

import entresimserver.Runnables.Queues.CustomDecisionHandler;
import entresimserver.Runnables.Queues.JointAcknowledger;
import entresimserver.Runnables.Queues.PairAcknowledger;
import entresimserver.Runnables.Queues.JointRequester;
import entresimserver.Runnables.Queues.PairRequester;
import entreObj.Decision;
import entreObj.EData;
import entreObj.Environment;
import entreObj.PlayerDetails;
import entreObj.WinData;
import entresimserver.FXMLServerController;
import entresimserver.Objects.Data.ConditionalData;
import entresimserver.Objects.Handlers.DecisionHandler;
import entresimserver.Objects.Data.RequestData;
import entresimserver.Objects.Data.Switch;
import entresimserver.Objects.Handlers.GraphicsHandler;
import entresimserver.Objects.Servicers.DFS;
import entresimserver.Runnables.Queues.JointExpecting;
import entresimserver.Runnables.Queues.PairExpecting;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author dylanleong
 */
public class GameController implements Runnable {

    private FXMLServerController server_controller;
    private PairRequester pair_requester;
    private PairExpecting pair_expecting;
    private PairAcknowledger pair_acknowledger;
    private CustomDecisionHandler custom_decision_handler;
    private JointRequester joint_requester;
    private JointExpecting joint_expecting;
    private JointAcknowledger joint_acknowledger;
    private ArrayList<String> names;
    private ArrayList<PlayerDetails> players;
    private TreeSet<Decision> decisions;
    private ArrayList<Switch> switches;
    private ArrayList<ConditionalData> conditional_data;
    private TreeSet<WinData> windata;
    private ArrayList<ServerOutStream> outstreams;
    private ArrayList<ServerInStream> instreams;
    private HashMap<Integer, Integer> id_remapper;
    private GraphicsHandler graphics_handler;
    public Environment env;
    private String room_name;
    private int rounds_max;
    private int remaining_players;
    private DecisionHandler decision_handler;
    private volatile CountDownLatch latch;
    private volatile CountDownLatch queue_latch;
    private int current_custom_id;
    public final boolean loaded;
    public final int starting_round;
    public volatile int current_round;

    /**
     * Use this constructor for new games.
     *
     * @param o array of outstreams
     * @param i array of instreams
     * @param sc parent controller for server
     * @param d decisions
     * @param rn room name
     */
    public GameController(ArrayList<ServerOutStream> o, ArrayList<ServerInStream> i, FXMLServerController sc, TreeSet<Decision> d, ArrayList<Switch> s, ArrayList<ConditionalData> cd, String rn, int rm) {
        names = new ArrayList<String>();
        players = new ArrayList<PlayerDetails>();
        windata = new TreeSet<WinData>();
        custom_decision_handler = new CustomDecisionHandler(this);
        decisions = d;
        switches = s;
        conditional_data = cd;
        decision_handler = new DecisionHandler(decisions, switches, conditional_data);
        room_name = rn;
        rounds_max = rm;
        starting_round = 0;
        current_round = 0;
        remaining_players = 0; //This value will be changed when thread runs

        outstreams = o;
        instreams = i;
        server_controller = sc;
        current_custom_id = -1;

        loaded = false;
        graphics_handler = new GraphicsHandler();
    }

    /**
     * Use this constructor for loaded games.
     *
     * @param o outstream array
     * @param i instream array
     * @param sc servercontroller
     * @param d decisions
     * @param rn room_name
     * @param e environment
     * @param dh decision_handler
     * @param wd windata
     * @param rm rounds max
     * @param current_round current round
     */
    public GameController(ArrayList<ServerOutStream> o, ArrayList<ServerInStream> i, FXMLServerController sc, TreeSet<Decision> d, ArrayList<Switch> s, ArrayList<ConditionalData> cd, String rn, Environment e, DecisionHandler dh, TreeSet<WinData> wd, int rm, int current_round) {
        names = new ArrayList<String>();
        env = e;
        players = e.players;
        windata = wd;
        custom_decision_handler = new CustomDecisionHandler(this);
        decisions = d;
        switches = s;
        conditional_data = cd;
        System.out.println("\n\n\nPrinting decisions:\n\n\n");
        for (Decision each : decisions) {
            System.out.println(each.title);
        }
        decision_handler = dh;
        room_name = rn;
        rounds_max = rm;
        remaining_players = 0; //This value will be changed when thread runs but is here for safety
        starting_round = current_round;
        this.current_round = current_round;
        id_remapper = new HashMap<Integer, Integer>();

        outstreams = o;
        instreams = i;
        server_controller = sc;
        current_custom_id = -1;

        loaded = true;
        graphics_handler = new GraphicsHandler();
    }

    @Override
    public void run() { //Thread should only start when all clients are ready

        try {
            PrintStream out;
            out = new PrintStream(new FileOutputStream("../../../server_error_log.txt"));
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!loaded) { //FOR NEW GAMES
            remaining_players = players.size();

            System.out.println(players);
            env = new Environment(players);
            for (PlayerDetails each_player : players) {
                windata.add(new WinData(each_player));
                for (Switch each_switch : switches) {
                    each_player.addSwitch(each_switch.getName());
                }
            }
            System.out.println("Windata size: " + windata.size());
        } else { //FOR LOADED GAMES
            remaining_players = instreams.size();
            //REMAP IDS OF PLAYERS, ETC
            for (ServerInStream each_instream : instreams) {
                each_instream.updateID(id_remapper.get(each_instream.getID()));
                System.out.println("remapped " + each_instream.getID() + " to " + id_remapper.get(each_instream.getID()));
            }
            for (ServerOutStream each_outstream : outstreams) {
                each_outstream.updateID(id_remapper.get(each_outstream.getID()));
            }
        }

        Platform.runLater(() -> {
            server_controller.setupCanvas(env);
        });

        custom_decision_handler.pullLatch();
        Thread t = new Thread(custom_decision_handler);
        t.setDaemon(true);
        t.start();

        pair_expecting = new PairExpecting(this, env);
        Thread pair_exp_thread = new Thread(pair_expecting);
        pair_exp_thread.setDaemon(true);
        pair_exp_thread.start();

        pair_requester = new PairRequester(this, env, pair_expecting);
        Thread pair_req_thread = new Thread(pair_requester);
        pair_req_thread.setDaemon(true);
        pair_req_thread.start();

        pair_acknowledger = new PairAcknowledger(this, env, pair_expecting);
        Thread pair_ack_thread = new Thread(pair_acknowledger);
        pair_ack_thread.setDaemon(true);
        pair_ack_thread.start();

        joint_expecting = new JointExpecting(this, env);
        Thread joint_exp_thread = new Thread(joint_expecting);
        joint_exp_thread.setDaemon(true);
        joint_exp_thread.start();

        joint_requester = new JointRequester(this, env, joint_expecting);
        Thread joint_req_thread = new Thread(joint_requester);
        joint_req_thread.setDaemon(true);
        joint_req_thread.start();

        joint_acknowledger = new JointAcknowledger(this, env, joint_expecting);
        Thread joint_ack_thread = new Thread(joint_acknowledger);
        joint_ack_thread.setDaemon(true);
        joint_ack_thread.start();

        sendAll("ROUND_DATA", env);
        sendAll("COMMENT", "Choose which of your resources you want to show/hide from other players and click end turn when ready.");
        sendAll("NEW_ROUND", 0); //The zero round is unique where no decisions are made, only resource hide toggles are changed.
        sendDecisions();
        setupLatch(remaining_players);
        updateShow(env);
        writeToLog("");

        for (int i = starting_round; i < rounds_max; i++) {
            current_round = i;

            //Autosave each round
            ArrayList<Object> save = new ArrayList<Object>();
            save.add(env); //0
            save.add(decision_handler); //1
            save.add(decisions); //2
            save.add(switches); //3
            save.add(conditional_data); //4
            save.add(windata); //5
            save.add(rounds_max); //6
            save.add((i + 1)); //7
            save.add(room_name); //8
            //I can't think of a cleaner way to do this... (except making another class to do it)

            try {
                File file = new File("../../../Saves/save_" + i + ".sav"); //
                //File file = new File("./Saves/save_" + i + ".sav");
                file.getParentFile().mkdirs();
                file.createNewFile();
                System.out.println(file.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream output = new ObjectOutputStream(fos);
                output.writeObject(save);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
            }

            final String round_num = "" + i;
            Platform.runLater(() -> {
                server_controller.saveLog(round_num); //also write to log each round.
            });

            System.out.println(env);
            sendAll("ROUND_DATA", env);
            writeToLog("ROUND " + (i + 1) + "\n");
            sendAll("NEW_ROUND", (i + 1));

            Platform.runLater(() -> {
                server_controller.updateRound();
                server_controller.redrawNetwork();
            });

            setupLatch(remaining_players); //Waits for all players to lock before continuing
            setupQueueLatches(); //Wait for all remaining joint/pair/etc queues to send interchanging requests            

            for (ServerInStream in : instreams) { //UPDATE DECISIONS
                if (!in.getDead()) {
                    String message = decision_handler.parseDecision(in.getID(), in.getChoice(), env, outstreams);
                    if (!message.equals("")) {
                        sendSingle(in.getID(), "MESSAGE", message);
                        sendSingle(in.getID(), "COMMENT", message);
                    }
                }
            }

            decision_handler.cycleEffects(env, this); //Pivotal function.  Applies ALL effects of each decision and handles the delay_queue

            String name = "";
            for (ServerInStream in : instreams) {
                for (PlayerDetails p : players) {
                    if (p.id == in.getID()) {
                        name = p.name;
                        writeToLog("Player " + name + " made the decision: " + decision_handler.getDecisionTitle(in.getChoice()));
                        writeToLog("They have the following resources:");
                        writeToLog("Finance : " + p.finance);
                        writeToLog("Exploratory Knowledge: " + p.explore);
                        writeToLog("Exploitative Knowledge: " + p.exploit);
                        writeToLog("Finance income/outcome per turn: " + p.f_change);
                        writeToLog("Exploratory knowledge change per turn: " + p.explore_change);
                        writeToLog("Exploitative knowledge change per turn: " + p.exploit_change);
                        writeToLog("\n");
                        break;
                    }
                }
            }

            for (PlayerDetails p : env.players) { //Do end of turn resource-per-turn changes
                p.resetJoint();
                if (!p.getDead()) { //Only change if not dead (avoid bringing dead players back to life)
                    p.finance += p.f_change;
                    p.explore += p.explore_change;
                    p.exploit += p.exploit_change;
                } else {
                    continue; //Ensure we only mark as dead then broadcast death once
                }
                if (p.finance <= 0) { //mark as dead if wasn't before
                    markAsDead(p, p.id);
                    Platform.runLater(() -> {
                        server_controller.redrawNetwork();
                    });
                    sendAll("MESSAGE", "Player " + p.name + " has run out of financial resources and is unable to participate further.\n");
                    writeToLog("Player " + p.name + " has run out of financial resources and is unable to participate further.");
                }
            }

            updateShow(env);

        } //Repeat round until end of game

        calcWinData();

        sendAll("ROUND_DATA", env);

        setupWinLatches();

        terminate();
    }

    /**
     * Send data to every stream.
     *
     * @param type the prefix of the message
     * @param content
     */
    private void sendAll(String type, Object content) {
        for (ServerOutStream o : outstreams) {
            o.setData(new EData(type, content));
        }
    }

    /**
     * Send data to a specific player.
     *
     * @param id the id of the player we want to send data to
     * @param data
     */
    public void sendSingle(int id, String type, Object content) {
        for (ServerOutStream out : outstreams) {
            if (out.getID() == id) {
                out.setData(new EData(type, content));
                break;
            }
        }
    }

    /**
     * Sets up latches for each remaining alive player.
     *
     * @param remaining
     */
    private void setupLatch(int remaining) {
        latch = new CountDownLatch(remaining);
        System.out.println("Setting up latch of size: " + remaining);
        for (ServerInStream in : instreams) {
            if (!in.getDead()) {
                in.updateLatch(latch);
            } else { //for dead players
                in.forceLatch(); //just in case
            }
        }
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets up latches for expecting queues. Only continues when all expecting
     * queues are empty.
     */
    private void setupQueueLatches() {
        queue_latch = new CountDownLatch(2);
        joint_expecting.updateLatch(queue_latch);
        pair_expecting.updateLatch(queue_latch);
        try {
            queue_latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets up latches at the end of the game, sends win graphs, and only
     * continues when every client acknowledges.
     */
    private void setupWinLatches() {

        latch = new CountDownLatch(env.players.size());
        System.out.println("Setting up latch of size: " + env.players.size());
        for (ServerInStream in : instreams) {
            in.updateLatch(latch);
        }

        try {
            sendAll("END", windata);
            Platform.runLater(() -> {
                server_controller.saveLog("end");
                server_controller.winGraph(graphics_handler.winGraph(windata, env));
            });
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(GameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void calcWinData() {
        DFS dfs_servicer = new DFS();
        ArrayList<ArrayList<Integer>> connected_groups = dfs_servicer.getJointGroups(env);
        System.out.println(connected_groups);

        for (WinData each_windata : windata) {
            PlayerDetails corresponding_player = env.int_to_player.get(each_windata.player_id);
            each_windata.calculateChange(env.players, corresponding_player, connected_groups);
        }
    }

    public void calcResourcesAndShowGraph() {
        calcWinData();
        Platform.runLater(() -> {
            server_controller.winGraph(graphics_handler.winGraph(windata, env));
        });
    }

    /**
     * Kills all streams.
     */
    private void closeAllStreams() {
        for (ServerInStream in : instreams) {
            in.terminate();
        }
    }

    public void markAsDead(PlayerDetails p, int ID) {
        p.setDead();
        for (ServerInStream each : instreams) {
            if (each.getID() == ID) {
                each.setDead();
                break;
            }
        }
        for (ServerOutStream each : outstreams) {
            if (each.getID() == ID) {
                each.setDead();
                break;
            }
        }
        remaining_players--;
        p.finance = 0;
    }

    private void updateShow(Environment env) {
        for (PlayerDetails p : env.players) {
            for (ServerInStream s : instreams) {
                if (p.id == s.getID() && !s.getDead()) {
                    p.show_finance = s.getShow()[0];
                    p.show_explore = s.getShow()[1];
                    p.show_exploit = s.getShow()[2];
                    break;
                }
            }
        }
    }

    public synchronized void approveName(String name, int id) {
        if (!loaded) { //FOR NEW GAMES
            String approval;
            if ((!names.contains(name)) && !name.trim().equals("") && name.length() <= 20) {
                names.add(name);
                players.add(new PlayerDetails(id, name));
                approval = "YES";
            } else {
                approval = "NO";
            }
            sendSingle(id, "NAME_ACK", approval);
            if (approval.equals("YES")) {
                Platform.runLater(() -> {
                    server_controller.writeTxtLog("Approved name " + name + ": " + approval);
                });
            }
        } else { //FOR LOADED GAMES
            //Check name against player list and name list
            boolean in_player_list = false;
            boolean in_name_list = false;
            PlayerDetails matched_player = null;
            for (PlayerDetails each_player : players) {
                if (each_player.name.equals(name)) {
                    in_player_list = true;
                    matched_player = each_player;
                    break;
                }
            }
            for (String each_name : names) {
                if (each_name.equals(name)) {
                    in_name_list = true;
                    break;
                }
            }
            if (in_player_list && !in_name_list) { //Valid player, existed in previous game and has not joined yet
                names.add(name); //So we can't get duplicate players
                sendSingle(id, "NAME_ACK", "YES");
                //MAP LOADED PLAYER ID TO NEW ID >> 
                id_remapper.put(id, matched_player.id);
            } else {
                //close connection with a message
                sendSingle(id, "NAME_ACK", "NO_LOAD");
            }

        }
    }

    public boolean checkReady() {
        if (!loaded) { //New Game
            return (names.size() == outstreams.size());
        } else { //Loaded Game
            System.out.println("Doing a ready check");
            if (outstreams.size() == players.size()) { //No problems, all students joined
                return true;
            } else { //If not all students join, we give the tutor the option to 'kill' unjoined students
                boolean killmissingplayers = server_controller.killMissingPlayers();
                if (killmissingplayers) { //do it
                    for (PlayerDetails each : players) {
                        if (!names.contains(each.name)) {
                            each.setDead();
                        }
                    }
                }
                return killmissingplayers;
            }
        }
    }

    public void sendDecisions() {
        System.out.println("SENDING DECISIONS");
        sendAll("CHOICES", decisions);
    }

    public void writeToLog(String log) {
        Platform.runLater(() -> {
            server_controller.writeTxtLog(log);
        });
    }

    public synchronized void pairRequest(String req) {
        pair_requester.addToQueue(req);
    }

    public synchronized void pairAcknowledge(String ack) {
        pair_acknowledger.addToQueue(ack);
    }

    public synchronized void jointRequest(String req) {
        joint_requester.addToQueue(req);
    }

    public synchronized void jointAcknowledge(String ack) {
        joint_acknowledger.addToQueue(ack);
    }

    public void parseJointDecision(int requester_id, int replier_id, int decision_id) {
        String message = decision_handler.parseDecision(requester_id, replier_id, decision_id, env, outstreams);
        if (!message.equals("")) {
            sendSingle(requester_id, "MESSAGE", message);
            sendSingle(requester_id, "COMMENT", message);
            sendSingle(replier_id, "MESSAGE", message);
            sendSingle(requester_id, "COMMENT", message);

        }

        String requester_name = env.int_to_player.get(requester_id).name;
        String replier_name = env.int_to_player.get(replier_id).name;

        writeToLog(requester_name + " and " + replier_name + " made the joint decision '" + decision_handler.getDecisionTitle(decision_id) + "' together.");
    }

//CUSTOM
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    public int countDecisions() {
        return decisions.size();
    }

    public int findFreeDecisionID() {
        int free_id = 0;
        boolean found = false;

        while (!found) {

            for (Decision each_decision : decisions) {
                found = true;
                if (each_decision.id == free_id) { //loop will only end if id is unused
                    found = false;
                    break;
                }
            }

            if (found) {
                break;
            } else {
                free_id++;
            }
        } //endwhile
        return free_id;
    }

    public void customSet(int id, String[] s) {
        System.out.println("proposal received.");
        custom_decision_handler.addToQueue(id, s);
    }

    public void customReceive(RequestData request) {
        current_custom_id = request.id;
        Platform.runLater(() -> {
            server_controller.writeTxtProposal(request.name, request.description);
            server_controller.notifyProposal();
        });
    }

    public void customReject(String comment) {
        if (current_custom_id < 0) {
            return; //ensure we don't try sending to a non-existing stream 
        }

        if (!comment.equals("")) {
            sendSingle(current_custom_id, "COMMENT", "Your proposal has been rejected.  The server's response: " + comment);
        } else {
            sendSingle(current_custom_id, "COMMENT", "Your proposal has been rejected.");
        }
        custom_decision_handler.pullLatch();
    }

    public void customAccept(String comment, Decision d) {
        if (current_custom_id < 0) {
            return; //ensure we don't try sending to a non-existing stream 
        }

        System.out.println("\n\nBEFORE ADDING");
        for (Decision each : decisions) {
            System.out.println(each.title);
        }

        decisions.add(d); //Add decision to decision set
        System.out.println("\n\nAFTER ADDING");
        for (Decision each : decisions) {
            System.out.println(each.title);
        }
        /* if (!decision_handler.decisions.contains(d)) {
            decision_handler.decisions.add(d); //Also add to handler if does not exist already
        }*/
        System.out.println("\n\nCHECKING HANDLER");
        for (Decision each : decision_handler.decisions) {
            System.out.println(each.title);
        }

        if (!comment.equals("")) {
            sendSingle(current_custom_id, "COMMENT", "Your proposal has been accepted.  The server's response: " + comment);
        } else {
            sendSingle(current_custom_id, "COMMENT", "Your proposal has been accepted.");
        }
        TreeSet<Decision> d_list = new TreeSet<Decision>();
        d_list.add(d);

        for (ServerOutStream out : outstreams) {
            out.setData(new EData("CHOICES", d_list));
        }

        custom_decision_handler.pullLatch();
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
//CUSTOM

    public void terminate() {
        joint_acknowledger.terminate();
        joint_requester.terminate();
        joint_expecting.terminate();
        pair_acknowledger.terminate();
        pair_requester.terminate();
        pair_expecting.terminate();
        for (ServerInStream in : instreams) {
            in.terminate();
        }
        for (ServerOutStream out : outstreams) {
            out.terminate();
        }
    }
}
