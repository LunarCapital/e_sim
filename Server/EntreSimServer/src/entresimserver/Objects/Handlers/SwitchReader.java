/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Handlers;

import entresimserver.Objects.Data.Switch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser for the conditionals_switch file.  Fills up an array of switches linking id to switch name.
 * @author dylanleong
 */
public class SwitchReader {
    
    public ArrayList<Switch> switches;
    
    public SwitchReader(ArrayList<Switch> s) {
        switches = s;
    }
    
    public void readSwitches(String path, String filename) {
        Path file = Paths.get(path + filename);
        try (InputStream in = Files.newInputStream(file)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String line = "";
            int expect = 0; //0=ID, 1=Name
            
            int id = 0;
            String name = "";
            
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue; //empty line, ignore
                } else if (line.charAt(0) == '/') {
                    continue; //comment line, ignore
                }
                
                if (expect == 0) { //ID
                    id = Integer.parseInt(line);
                    expect = 1;
                }
                else if (expect == 1) {
                    name = line;
                    expect = 0;
                    switches.add(new Switch(id, name));
                }
                
            }
            
        } catch (IOException ex) {
            Logger.getLogger(SwitchReader.class.getName()).log(Level.SEVERE, "Unable to read switches file.", ex);
        }
       
    }
}
