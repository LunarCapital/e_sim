/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Handlers;

import entreObj.Environment;
import entreObj.Decision;
import entreObj.EData;
import entreObj.PlayerDetails;
import entresimserver.Objects.Data.ConditionalData;
import entresimserver.Objects.Data.EffectData;
import entresimserver.Objects.Data.Switch;
import entresimserver.Runnables.GameController;
import entresimserver.Runnables.ServerOutStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * An object that has the sole purpose of handling decisions and their effects.
 * (It's a pretty big purpose) When a player calls a decision, it gets sent
 * here, and its effects are extracted and placed on a queue in the Delay
 * Handler. Each round the Delay Handler is checked and effects with 0 delay are
 * sent back to this object and applied to the Environment object.
 *
 * @author dylanleong
 */
public class DecisionHandler implements Serializable {

    public TreeSet<Decision> decisions;
    public ArrayList<Switch> switches;
    public ArrayList<ConditionalData> conditional_data;
    private DelayHandler delay_handler;
    private HashMap<String, String> shorthand;

    public DecisionHandler(TreeSet<Decision> d, ArrayList<Switch> s, ArrayList<ConditionalData> cd) {
        decisions = d;
        switches = s;
        conditional_data = cd;
        delay_handler = new DelayHandler();
        shorthand = new HashMap<String, String>();
        initializeShorthand();
    }

    /**
     * Prepare a decision's effects to be placed on the delay queue. This
     * function is for individual decisions.
     *
     * @param applicant_id id of the applicant
     * @param decision_id id of the decision
     * @param env the environment
     * @return a message to the client, in the case of probability rolls.
     */
    public String parseDecision(int applicant_id, int decision_id, Environment env, ArrayList<ServerOutStream> outstreams) {
        Decision d = getDecision(decision_id);

        StringBuilder sb = new StringBuilder("");

        String type = "applicant";
        int recipient_id = -1;

        ArrayList<String> dummy_applicant_effects = new ArrayList<>();
        dummy_applicant_effects.addAll(d.applicant_effects);
        dummy_applicant_effects = changeConditional(decision_id, applicant_id, env, dummy_applicant_effects, "applicant");

        for (String effect : dummy_applicant_effects) {
            String message_add = queueEffectTokens(type, effect, d.title, applicant_id, recipient_id, env, decision_id, outstreams);
            if (!message_add.equals("")) {
                sb.append(message_add);
            }
        }

        return sb.toString();
    }

    /**
     * Prepare a decision's effects to be placed on the delay queue. This
     * function is for joint decisions.
     *
     * @param applicant_id id of the applicant
     * @param recipient_id id of the recipient
     * @param decision_id id of the decision
     * @param env the environment object
     * @return a message back to BOTH players involved if probability is
     * involved
     */
    public String parseDecision(int applicant_id, int recipient_id, int decision_id, Environment env, ArrayList<ServerOutStream> outstreams) {
        Decision d = getDecision(decision_id);

        StringBuilder sb = new StringBuilder("");

        String type = "applicant";
        ArrayList<String> dummy_applicant_effects = new ArrayList<>();
        dummy_applicant_effects.addAll(d.applicant_effects);
        dummy_applicant_effects = changeConditional(decision_id, applicant_id, env, dummy_applicant_effects, type);

        for (String effect : dummy_applicant_effects) {
            String message_add = queueEffectTokens(type, effect, d.title, applicant_id, recipient_id, env, decision_id, outstreams);
            if (!message_add.equals("")) {
                sb.append(message_add);
            }
        }

        type = "recipient";
        ArrayList<String> dummy_recipient_effects = new ArrayList<>();
        dummy_recipient_effects.addAll(d.recipient_effects);
        dummy_recipient_effects = changeConditional(decision_id, applicant_id, env, dummy_recipient_effects, type);

        for (String effect : dummy_recipient_effects) {
            String message_add = queueEffectTokens(type, effect, d.title, applicant_id, recipient_id, env, decision_id, outstreams);
            if (!message_add.equals("")) {
                sb.append(message_add);
            }
        }

        type = "both";
        ArrayList<String> dummy_both_effects = new ArrayList<>();
        dummy_both_effects.addAll(d.both_effects);
        dummy_both_effects = changeConditional(decision_id, applicant_id, env, dummy_both_effects, type);

        System.out.println(dummy_both_effects);

        for (String effect : dummy_both_effects) {
            String message_add = queueEffectTokens(type, effect, d.title, applicant_id, recipient_id, env, decision_id, outstreams);
            if (!message_add.equals("")) {
                sb.append(message_add);
            }
        }

        return sb.toString();
    }

    /**
     * A simple function that returns a Decision's title based off a given ID.
     *
     * @param decision_id the ID in question
     * @return the title of the Decision, or null if we find none.
     */
    public String getDecisionTitle(int decision_id) {
        for (Decision d : decisions) {
            if (d.id == decision_id) {
                return d.title;
            }
        }
        return null;
    }

    /**
     * Cycle effects in the delay handler. Anything with delay = 0 should be
     * made effective, while anything with delay > 0 will have its delay reduced
     * and be put back on the queue for the next round.
     *
     * @param env
     * @param game_controller
     */
    public void cycleEffects(Environment env, GameController game_controller) {
        ArrayList<EffectData> effective = delay_handler.cycleQueue();

        for (EffectData each_effect : effective) {
            if (!relatedPlayersAlive(env, each_effect)) {
                continue;
            }

            PlayerDetails applicant = env.getPlayer(each_effect.applicant_id);
            PlayerDetails recipient = (each_effect.recipient_id != -1) ? env.getPlayer(each_effect.recipient_id) : null;

            //grab type of effect
            switch (each_effect.type) {
                case "applicant":
                    if (!applicant.getDead()) {
                        commitChanges(applicant, each_effect, env, game_controller);
                    }
                    break;
                case "recipient":
                    if (recipient != null) {
                        if (!recipient.getDead()) {
                            commitChanges(recipient, each_effect, env, game_controller);
                        }
                    }
                    break;
                case "both":
                    if (!applicant.getDead()) {
                        commitChanges(applicant, each_effect, env, game_controller);
                    }
                    if (recipient != null) {
                        if (!recipient.getDead()) {
                            commitChanges(recipient, each_effect, env, game_controller);
                        }
                    }
                    break;
                case "join":
                    System.out.println("SPECIAL CASE JOIN");
                    specialCaseJoin(each_effect, env, game_controller);
                    break;
                case "jointgrant":
                    specialCaseJointGrant(each_effect, env, game_controller);
                    break;
                default:
                    break;
            }

        }
        //if join or jointgrant do something special
    }

    /**
     * A fairly heavy function that queues effects on the delay handler using
     * the effect arrays in a decision.
     *
     * @param type of effect, whether it's applicant, recipient, or both.
     * @param effect being what resource is affected, like finance, explore,
     * etc.
     * @param title of the decision that was made
     * @param applicant_id applicant who made the decision
     * @param recipient_id who received the decision, only applicable in joint
     * decisions (otherwise set to -1)
     * @return a message String that will be relayed to the players involved
     */
    private String queueEffectTokens(String type, String effect, String title, int applicant_id, int recipient_id, Environment env, int decision_id, ArrayList<ServerOutStream> outstreams) {
        StringTokenizer st = new StringTokenizer(effect);

        if (st.countTokens() > 4) { //Probability case
            st.nextToken(); //Skip past the first "P"
            double probability = Double.parseDouble(st.nextToken());

            Random rand = new Random();
            double roll = rand.nextDouble(); //success if roll <= probability
            boolean success = false;
            if (roll <= probability && probability > 0) {
                success = true;
            } else if (roll > probability) {
                success = false;
            }

            String message = ""; //Most of this function is just padding the message string.  
            type = type.trim();
            if (type.equals("applicant")) {
                message += "The applicant's";
            } else if (type.equals("recipient")) {
                message += "The recipient's";
            } else if (type.equals("both")) {
                message += "Your";
            }
            message += " probability roll of " + probability + " for decision \"" + title + "\" ";
            message += (success) ? "succeeded!" : "failed.";
            message += " The outcome";
            message += (success) ? " involves the following change(s):\n" : " would have involved the following change(s):\n";

            do { //continue for every effect
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                if (success) {
                    EffectData new_effect = createEffect(type, resource, magnitude, delay, linked, applicant_id, recipient_id);
                    env.getPlayer(applicant_id).successful_decisions.add(decision_id);
                    switchConditional(decision_id, applicant_id, env, outstreams); //switch conditional on if necessary
                    if (recipient_id != -1) {
                        env.getPlayer(recipient_id).successful_decisions.add(decision_id);
                        switchConditional(decision_id, recipient_id, env, outstreams);
                    }
                    delay_handler.placeOnQueue(new_effect);
                }

                message += shorthand.get(resource) + " would change by " + magnitude + " in " + delay + " turn(s).";
            } while (st.countTokens() >= 4);

            if (probability == 1) {
                message = "";
            }
            return message;

        } else if (st.countTokens() == 4) { //Normal case
            String resource = st.nextToken();
            String magnitude = st.nextToken();
            String delay = st.nextToken();
            String linked = st.nextToken();

            if (!(magnitude.equals("0"))) { //zero magnitude changes are used as placeholders for "no change" in the textfile
                EffectData new_effect = createEffect(type, resource, magnitude, delay, linked, applicant_id, recipient_id);
                env.getPlayer(applicant_id).successful_decisions.add(decision_id);
                switchConditional(decision_id, applicant_id, env, outstreams);
                if (recipient_id != -1) {
                    env.getPlayer(recipient_id).successful_decisions.add(decision_id);
                    switchConditional(decision_id, recipient_id, env, outstreams);
                }
                delay_handler.placeOnQueue(new_effect);
            }
            return "";
        } else if (st.countTokens() == 1) { //SPECIAL CASE
            String special = st.nextToken();

            EffectData new_effect = createEffect(type, special, "0", "0", "false", applicant_id, recipient_id);
            env.getPlayer(applicant_id).successful_decisions.add(decision_id);
            switchConditional(decision_id, applicant_id, env, outstreams);
            if (recipient_id != -1) {
                env.getPlayer(recipient_id).successful_decisions.add(decision_id);
                switchConditional(decision_id, applicant_id, env, outstreams);
            }
            delay_handler.placeOnQueue(new_effect);
            return "";
        }
        return "";
    }

    /**
     * Creates an EffectData object based off the variables given.
     *
     * @param type of effect, applicant/recipient/both
     * @param resource involved, finance/explore/etc
     * @param magnitude change of the resource
     * @param delay in rounds. If zero, the effect will occur immediately. A 1
     * round delay will take place next round.
     * @param linked to any FBs who invested. Fairly rare and heavily related to
     * the joint decision "Apply for Grants".
     * @param applicant_id applicant who made the decision
     * @param recipient_id recipient of the joint decision (or -1 if individual)
     * @return the new EffectData object.
     */
    private EffectData createEffect(String type, String resource, String magnitude, String delay, String linked, int applicant_id, int recipient_id) {
        EffectData new_effect;
        type = type.trim();
        resource = resource.trim();
        magnitude = magnitude.trim();
        delay = delay.trim();
        linked = linked.trim();
        if (resource.equals("join") || resource.equals("jointgrant")) {//Check special join or jointgrant cases
            new_effect = new EffectData(resource, resource, 0, false, applicant_id, recipient_id, 0);
        } else {
            new_effect = new EffectData(type, resource, Integer.parseInt(magnitude), Boolean.parseBoolean(linked), applicant_id, recipient_id, Integer.parseInt(delay));
        }
        return new_effect;
    }

    /**
     * Grabs a decision from the decision array based off the ID. Just in case
     * we ever have a situation where the text file has non-sequential IDs, etc.
     *
     * @param decision_id the ID of the decision we want to find
     * @return the corresponding decision with ID = decision_id
     */
    private Decision getDecision(int decision_id) {
        for (Decision each : decisions) {
            if (each.id == decision_id) {
                return each;
            }
        }
        return null; //This should never ever happen, and the game should stop if we got this far trying to call a decision_id that doesn't exist in the text file
    }

    /**
     * Perform resource changes on the specified player and linked parent
     * players if required.
     *
     * @param player , the player in question.
     * @param effect details of the effect
     * @param env and the environment for finding parent players via ID.
     */
    private void commitChanges(PlayerDetails player, EffectData effect, Environment env, GameController game_controller) {
        switch (effect.resource) {
            case "finance":
                player.finance += effect.magnitude;
                break;
            case "explore":
                player.explore += effect.magnitude;
                break;
            case "exploit":
                player.exploit += effect.magnitude;
                break;
            case "f_change":
                player.f_change += effect.magnitude;
                break;
            case "explore_change":
                player.explore_change += effect.magnitude;
                break;
            case "exploit_change":
                player.exploit_change += effect.magnitude;
                break;
            default:
                break;
        }

        if (effect.linked) { //do linked changes (i missed an opportunity for recursiveness here)
            for (Integer each_parent : player.parent_players) {
                PlayerDetails parent_player = env.getPlayer(each_parent);
                String message = "Your link to another player has led to a change in ";
                switch (effect.resource) {
                    case "finance":
                        parent_player.finance += effect.magnitude;
                        message += shorthand.get("finance");
                        break;
                    case "explore":
                        parent_player.explore += effect.magnitude;
                        message += shorthand.get("explore");
                        break;
                    case "exploit":
                        parent_player.exploit += effect.magnitude;
                        message += shorthand.get("exploit");
                        break;
                    case "f_change":
                        parent_player.f_change += effect.magnitude;
                        message += shorthand.get("f_change");
                        break;
                    case "explore_change":
                        parent_player.explore_change += effect.magnitude;
                        message += shorthand.get("explore_change");
                        break;
                    case "exploit_change":
                        parent_player.exploit_change += effect.magnitude;
                        message += shorthand.get("exploit_change");
                        break;
                    default:
                        break;
                }
                message += " by " + effect.magnitude + ".";
                game_controller.sendSingle(each_parent, "MESSAGE", message);
            } //endfor
        }//endif

    }

    /**
     * Checks that players are alive and returns true if both alive or false if
     * either or both are dead.
     *
     * @param env environment of players and network
     * @param applicant_id applicant of effect
     * @param recipient_id recipient of effect
     * @param effect effect in question
     * @return true if both alive, false if any of them are dead
     */
    private boolean relatedPlayersAlive(Environment env, EffectData effect) {
        boolean related_players_alive = true;
        if ((env.getPlayer(effect.applicant_id)).getDead()) {
            related_players_alive = false;
        }
        if (effect.recipient_id != -1) {
            if ((env.getPlayer(effect.recipient_id)).getDead()) {
                related_players_alive = false;
            }
        }
        return related_players_alive;
    }

    /**
     * The special case effect where two players initiate a Joint Venture. It's
     * considered special because one player (the one with less connections)
     * 'dies', the other player gains all their resources, and the two groups
     * will now have to sit together in real life to collaborate for further
     * decisions.
     */
    private void specialCaseJoin(EffectData effect, Environment env, GameController game_controller) {
        //count connections of each player
        int applicant_connections = 0;
        int recipient_connections = 0;
        for (int i = 0; i < env.players.size(); i++) {
            if (env.network[effect.applicant_id][i] != -1 && i != effect.applicant_id) {
                applicant_connections++;
            }
        }
        for (int i = 0; i < env.players.size(); i++) {
            if (env.network[effect.recipient_id][i] != -1 && i != effect.recipient_id) {
                recipient_connections++;
            }
        }

        int weaker_player_id = (applicant_connections < recipient_connections) ? effect.applicant_id : effect.recipient_id;
        int stronger_player_id = (applicant_connections < recipient_connections) ? effect.recipient_id : effect.applicant_id;

        //Send a message to both players indicating which player will take the other's resources
        String message = "Player " + env.getPlayer(stronger_player_id).name + " will take the combined resources of the two of you.";
        game_controller.sendSingle(effect.applicant_id, "MESSAGE", message);
        game_controller.sendSingle(effect.recipient_id, "MESSAGE", message);

        //move resources from weaker player to stronger player and mark weaker player as dead
        PlayerDetails weaker_player = env.getPlayer(weaker_player_id);
        PlayerDetails stronger_player = env.getPlayer(stronger_player_id);

        stronger_player.finance += weaker_player.finance;
        stronger_player.explore += weaker_player.explore;
        stronger_player.exploit += weaker_player.exploit;
        stronger_player.f_change += weaker_player.f_change;
        stronger_player.explore_change += weaker_player.explore_change;
        stronger_player.exploit_change += weaker_player.exploit_change;
        weaker_player.finance = 0;
        weaker_player.explore = 0;
        weaker_player.exploit = 0;
        weaker_player.f_change = 0;
        weaker_player.explore_change = 0;
        weaker_player.exploit_change = 0;
        
        env.joint_graph[weaker_player.id][stronger_player.id] = true;
        env.joint_graph[stronger_player.id][weaker_player.id] = true;       

        if ((stronger_player.getRole().equals("R") && weaker_player.getRole().equals("M")) || stronger_player.getRole().equals("M") && weaker_player.getRole().equals("R")) {
            stronger_player.jointRole();

            //MAKE SURE PLAYER RESETS THEIR BUTTONS
        }

        game_controller.markAsDead(weaker_player, weaker_player_id);
    }

    /**
     * The special case effect where two players carry out a Joint Application
     * for Grants. It's considered special because it involves a probability
     * roll, that if successful, designates the two players as being 'linked'.
     * The player who provided the grant is designated as the 'parent' player,
     * and gains potential benefits if their 'child' player makes decisions 10
     * or 11 (as of the time that this comment was written, only these two
     * decisions take advantage of the 'linked' status).
     */
    private void specialCaseJointGrant(EffectData effect, Environment env, GameController game_controller) {
        //Move 3 finance from recipient to applicant
        PlayerDetails applicant = env.getPlayer(effect.applicant_id);
        PlayerDetails recipient = env.getPlayer(effect.recipient_id);
        applicant.finance += 3;
        recipient.finance -= 3;

        //Do 0.5 probability roll
        Random rand = new Random();
        double roll = rand.nextDouble();

        if (roll <= 0.5) { //success
            //Grant recipient income of 1
            recipient.f_change += 1;
            //Update parent/child arraylists  
            applicant.parent_players.add(effect.recipient_id);
            recipient.child_players.add(effect.applicant_id);
            //Notify FB
            game_controller.sendSingle(effect.recipient_id, "MESSAGE", "Your investment into another player has paid off and you will receive an income of 1 financial unit per turn.  \nAdditionally, you may receive further bonuses if said player is successful.");
        } else {
            game_controller.sendSingle(effect.recipient_id, "MESSAGE", "Your investment into another player was unsuccessful.");
        }
    }

    /**
     * Switches on a conditional if a player makes a decision that does so.
     */
    private void switchConditional(int decision_id, int player_id, Environment env, ArrayList<ServerOutStream> outstreams) {
        for (Switch each_switch : switches) {
            if (each_switch.getId() == decision_id) {
                env.getPlayer(player_id).switch_map.replace(each_switch.getName(), Boolean.FALSE, Boolean.TRUE);
            }
        }

        //CHANGES TO SEND TO CLIENT
        ArrayList<Decision> updated_decisions = new ArrayList<Decision>();
        for (Decision each_decision : decisions) {
            if (each_decision.type.equals("Indiv")) {
                String type = "applicant";
                ArrayList<String> dummy_applicant_effects = new ArrayList<>();
                dummy_applicant_effects.addAll(each_decision.applicant_effects);
                dummy_applicant_effects = changeConditional(decision_id, player_id, env, dummy_applicant_effects, type);

                ArrayList<String> dummy_prerequisites = new ArrayList<>();
                dummy_prerequisites.addAll(each_decision.prereqs);
                Decision copy_decision = new Decision(each_decision.id, each_decision.title, each_decision.description, each_decision.type, dummy_prerequisites, dummy_applicant_effects, new ArrayList<String>(), new ArrayList<String>());
                updated_decisions.add(copy_decision);

            } else {
                String type = "applicant";
                ArrayList<String> dummy_applicant_effects = new ArrayList<>();
                dummy_applicant_effects.addAll(each_decision.applicant_effects);
                dummy_applicant_effects = changeConditional(decision_id, player_id, env, dummy_applicant_effects, type);

                type = "recipient";
                ArrayList<String> dummy_recipient_effects = new ArrayList<>();
                dummy_recipient_effects.addAll(each_decision.recipient_effects);
                dummy_recipient_effects = changeConditional(decision_id, player_id, env, dummy_recipient_effects, type);

                type = "both";
                ArrayList<String> dummy_both_effects = new ArrayList<>();
                dummy_both_effects.addAll(each_decision.both_effects);
                dummy_both_effects = changeConditional(decision_id, player_id, env, dummy_both_effects, type);

                ArrayList<String> dummy_prerequisites = new ArrayList<>();
                dummy_prerequisites.addAll(each_decision.prereqs);
                Decision copy_decision = new Decision(each_decision.id, each_decision.title, each_decision.description, each_decision.type, dummy_prerequisites, dummy_applicant_effects, dummy_recipient_effects, dummy_both_effects);
                updated_decisions.add(copy_decision);
            }

        }

        for (ServerOutStream each : outstreams) {
            if (each.getID() == player_id) {
                each.setData(new EData("DECISION_UPDATE", updated_decisions));
                break;
            }
        }
    }

    /**
     * Makes changes to a decision's effects specified by a conditional switch
     * (if one exists).
     *
     * @return
     */
    private ArrayList<String> changeConditional(int decision_id, int player_id, Environment env, ArrayList<String> dummy_effects, String type) {
        for (ConditionalData each_conditional : conditional_data) {
            if (each_conditional.decision_id == decision_id) { //if the decision is changed by a conditional
                if (env.getPlayer(player_id).switch_map.get(each_conditional.name)) { //and the player has the conditional switch on
                    if (type.equals("applicant")) {
                        if (each_conditional.applicant_type.equals("A")) {
                            if (!each_conditional.applicant_changes.contains("nothing")) {
                                dummy_effects.addAll(each_conditional.applicant_changes);
                            }
                        } else if (each_conditional.applicant_type.equals("R")) {
                            for (String each_change : each_conditional.applicant_changes) {
                                //use first token as index (minus one)
                                StringTokenizer st = new StringTokenizer(each_change);
                                String index_str = st.nextToken();
                                int index = Integer.parseInt(index_str);
                                String remaining_change = each_change.substring(index_str.length() + 1, each_change.length());
                                dummy_effects.set(index - 1, remaining_change);
                            }
                        }
                    } else if (type.equals("recipient")) {
                        if (each_conditional.recipient_type.equals("A")) {
                            if (!each_conditional.recipient_changes.contains("nothing")) {
                                dummy_effects.addAll(each_conditional.recipient_changes);
                            }
                        } else if (each_conditional.recipient_type.equals("R")) {
                            for (String each_change : each_conditional.recipient_changes) {
                                //use first token as index (minus one)
                                StringTokenizer st = new StringTokenizer(each_change);
                                String index_str = st.nextToken();
                                int index = Integer.parseInt(index_str);
                                String remaining_change = each_change.substring(index_str.length() + 1, each_change.length());
                                dummy_effects.set(index - 1, remaining_change);
                            }
                        }
                    } else if (type.equals("both")) {
                        if (each_conditional.both_type.equals("A")) {
                            if (!each_conditional.both_changes.contains("nothing")) {
                                dummy_effects.addAll(each_conditional.both_changes);
                            }
                        } else if (each_conditional.both_type.equals("R")) {
                            for (String each_change : each_conditional.both_changes) {
                                //use first token as index (minus one)
                                StringTokenizer st = new StringTokenizer(each_change);
                                String index_str = st.nextToken();
                                int index = Integer.parseInt(index_str);
                                String remaining_change = each_change.substring(index_str.length() + 1, each_change.length());
                                dummy_effects.set(index - 1, remaining_change);
                            }
                        }
                    }
                }
            }
        }
        return dummy_effects;
    }

    /**
     * Just a quick initialization of our hashmap for message writing
     */
    private void initializeShorthand() {
        shorthand.put("finance", "Financial resources");
        shorthand.put("explore", "Exploratory knowledge");
        shorthand.put("exploit", "Exploitative knowledge");
        shorthand.put("f_change", "Income");
        shorthand.put("explore_change", "Exploratory knowledge per turn");
        shorthand.put("exploit_change", "Exploitative knowledge per turn");
    }

}
