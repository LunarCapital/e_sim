/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver;

import entreObj.Decision;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import proposalclasses.ViewEffect;

/**
 * FXML Controller class
 *
 * @author dylanleong
 */
public class FXMLDecisionQuickViewController implements Initializable {

    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtType;
    @FXML
    private ListView<String> listPrereqs;
    @FXML
    private TableView<ViewEffect> tableEffects;
    @FXML
    private TableColumn<ViewEffect, String> colProbability;
    @FXML
    private TableColumn<ViewEffect, String> colAffected;
    @FXML
    private TableColumn<ViewEffect, String> colEffects;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void init(TreeSet<Decision> d_set, int decision_id) {
        Decision display_decision = getDecision(d_set, decision_id);

        txtTitle.setText(display_decision.title);
        txtType.setText(display_decision.type);
        txtDescription.setText(display_decision.description);

        ArrayList<String> prereqs_readable = convertPrereqsReadable(display_decision.prereqs, d_set);
        ObservableList<String> prereqs_list = FXCollections.observableArrayList(prereqs_readable);
        listPrereqs.setItems(prereqs_list);

        ArrayList<ViewEffect> effects_readable = new ArrayList<ViewEffect>();
        //Display effects
        if (display_decision.type.equals("Indiv")) { //INDIVIDUAL
            effects_readable.addAll(convertEffectsReadable(display_decision.applicant_effects, "Applicant"));
        } else { //JOINT
            effects_readable.addAll(convertEffectsReadable(display_decision.applicant_effects, "Applicant"));
            effects_readable.addAll(convertEffectsReadable(display_decision.recipient_effects, "Recipient"));
            effects_readable.addAll(convertEffectsReadable(display_decision.both_effects, "Both"));
        }

        ObservableList<ViewEffect> effects_list = FXCollections.observableArrayList(effects_readable);
        tableEffects.setItems(effects_list);
        colProbability.setCellValueFactory(new PropertyValueFactory("probability"));
        colAffected.setCellValueFactory(new PropertyValueFactory("affected"));
        colEffects.setCellValueFactory(new PropertyValueFactory("effects"));

        //Wrap text for long effects
        colEffects.setCellFactory(tc -> { //
            TableCell<ViewEffect, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colEffects.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    /**
     * Convert the abbreviated prereq string to something readable for the user.
     *
     * @param prereqs Array of prereqs specific to one decision
     * @param d_set The treeset of decisions (so we can find IDs for "D" and "N"
     * prereqs)
     * @return an arraylist of readable strings, one for each prereq.
     */
    private ArrayList<String> convertPrereqsReadable(ArrayList<String> prereqs, TreeSet<Decision> d_set) {
        ArrayList<String> readable = new ArrayList<String>();

        for (String each_prereq : prereqs) {
            if (each_prereq.trim().equals("none")) {
                readable.add("No prerequisites.");
                break; //break early if no prereqs
            }
            StringTokenizer st = new StringTokenizer(each_prereq);
            String type = st.nextToken();
            String magnitude;
            if (type.equals("joinprereq")) {
                magnitude = "0";
            } else {
                magnitude = st.nextToken();
            }

            switch (type) {
                case "D":
                    readable.add("Must have made decision '" + getDecision(d_set, Integer.valueOf(magnitude)).title + "'.");
                    break;
                case "N":
                    readable.add("Must not have made decision '" + getDecision(d_set, Integer.valueOf(magnitude)).title + "'.");
                    break;
                case "finance":
                    readable.add("Need at least " + magnitude + " units of finance.");
                    break;
                case "explore":
                    readable.add("Need at least " + magnitude + " of exploratory knowledge.");
                    break;
                case "exploit":
                    readable.add("Need at least " + magnitude + " of exploitative knowledge.");
                    break;
                case "a_role":
                    readable.add("Applicant must be a " + getRole(magnitude) + ".");
                    break;
                case "r_role":
                    readable.add("Recipient must be a " + getRole(magnitude) + ".");
                    break;
                case "a_n_role":
                    readable.add("Applicant can be any role except " + getRole(magnitude) + ".");
                    break;
                case "r_n_role":
                    readable.add("Recipient can be any role except " + getRole(magnitude) + ".");
                    break;
                case "joinprereq":
                    readable.add("Joint Decision prerequisites:\n8 exploitative knowledge\nThe applicant and recipient's resources summed together must exceed 20\nAt least one player needs more than 10 finance\nThe two players must either be the same role, or an R&D-Manufacturer pair ");
                    break;
                default:
                    break;
            }

        }

        return readable;
    }

    /**
     * Convert the abbreviated effects string to a more readable form.
     *
     * @param effects is the arraylist of abbreviated effects
     * @return an arraylist of readable effects
     */
    private ArrayList<ViewEffect> convertEffectsReadable(ArrayList<String> effects, String type) {
        ArrayList<ViewEffect> readable_effects = new ArrayList<ViewEffect>();

        if (effects.size() == 1 && effects.contains("finance 0 0 false")) { //this is an empty effect that we shouldn't bother showing
            return readable_effects;
        }

        //LOOP FOR EVERY EFFECT IN ARRAYLIST AND STRINGTOKENIZE
        for (String each_effect : effects) {
            ViewEffect ve = new ViewEffect();
            ve.setAffected(type.trim()); //applicant, recipient, both
            StringTokenizer st = new StringTokenizer(each_effect);
            if (st.countTokens() > 4) { //Probability case
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();
                ve.setProbability(probability);

                StringBuilder effect = new StringBuilder("");
                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    effect.append("Changes " + getResource(resource) + " by " + magnitude + " ");
                    if (delay.equals("0")) {
                        effect.append("instantly ");
                    } else {
                        effect.append("in " + delay + " turns");
                    }
                    if (linked.equals("true")) {
                        effect.append(" and applies the same effect on any linked Financial Backers.  ");
                    } else {
                        effect.append(".  ");
                    }

                } while (st.countTokens() >= 4);

                ve.setEffect(effect.toString());
                readable_effects.add(ve);

            } else if (st.countTokens() == 4) { //Normal case
                ve.setProbability("1.0");
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                StringBuilder effect = new StringBuilder("");
                effect.append("Changes " + getResource(resource) + " by " + magnitude + " ");
                if (delay.equals("0")) {
                    effect.append("instantly ");
                } else {
                    effect.append("in " + delay + " turns");
                }
                if (linked.equals("true")) {
                    effect.append(" and applies the same effect on any linked Financial Backers.  ");
                } else {
                    effect.append(".  ");
                }
                ve.setEffect(effect.toString());
                readable_effects.add(ve);

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                ve.setProbability("1.0");
                String special = st.nextToken();
                String effect = "";

                if (special.equals("join")) {
                    effect = "Initiates a joint venture.";
                } else if (special.equals("jointgrant")) {
                    ve.setProbability("0.5");
                    effect = "The applicant is given 3 units of finance by the recipient.  If the probability roll is successful, grants the recipient 1 income and links them together with the applicant, allowing the recipient to gain benefits from linked decisions.";
                }

                ve.setEffect(effect);
                readable_effects.add(ve);
            }
        }
        return readable_effects;
    }

    /**
     * Grab a decision via an ID.
     *
     * @param id of decision
     * @return corresponding decision
     */
    private Decision getDecision(TreeSet<Decision> d, int decision_id) {
        for (Decision each : d) {
            if (each.id == decision_id) {
                return each;
            }
        }
        return null; //This should never ever happen
    }

    /**
     * Change one letter role abbreviations to their full name.
     *
     * @param r the abbreviation
     * @return the full name of the role
     */
    private String getRole(String r) {
        if (r.equals("R")) {
            return "R&D";
        } else if (r.equals("M")) {
            return "manufacturer";
        } else if (r.equals("F")) {
            return "financial backer";
        } else {
            return null; //Hopefully we never ever see this (maybe I should end the program if this is the case)
        }
    }

    /**
     * Change abbreviated resource names to their full name.
     */
    private String getResource(String r) {
        if (r.equals("finance")) {
            return "finance";
        } else if (r.equals("explore")) {
            return "exploratory knowledge";
        } else if (r.equals("exploit")) {
            return "exploitative knowledge";
        } else if (r.equals("f_change")) {
            return "income";
        } else if (r.equals("explore_change")) {
            return "exploratory knowledge per turn";
        } else if (r.equals("exploit_change")) {
            return "exploitative knowledge per turn";
        } else {
            return null;
        }
    }

    @FXML
    private void handleBtnClose(ActionEvent event) {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }
}
