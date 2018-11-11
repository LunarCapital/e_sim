/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entreObj;

/**
 * The data that gets sent between the server and client.
 * Contains of a type prefix, so streams know what to expect.
 * The content is set to object so any data can be placed in the field.
 * @author dylanleong
 */
public class EData {
    
    private String type;
    private Object content;
    
    public EData(String t, Object c) {
        type = t;
        content = c;
    }
    
    public String getType() {
        return type;
    }
    
    public Object getObject() {
        return content;
    }
}
