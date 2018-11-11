/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Data;

/**
 *
 * @author dylanleong
 */
public class RequestData {
    
    public final int id;
    public final String name;
    public final String description;
    
    public RequestData(int i, String n, String d) {
        id = i;
        name = n;
        description = d;
    }
    
}
