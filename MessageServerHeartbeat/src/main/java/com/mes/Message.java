package com.mes;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

public class Message {
    protected final static String FILENAME = "resources/fix4_2.xml";
    protected final static List<String> CONTINUEKEYS = new ArrayList<>(Arrays.asList("header", "CheckSum"));
    //protected final static List<String> CONTINUEKEYS = new ArrayList<>(Arrays.asList(new String[]{"header", "CheckSum"}));
    protected final static List<String> BIGCONTINUEKEYS = new ArrayList<>(Arrays.asList("BeginString", "BodyLength", "MsgType", "CheckSum"));
    //protected final static List<String> BIGCONTINUEKEYS = new ArrayList<>(Arrays.asList(new String[]{"BeginString", "BodyLength", "MsgType", "CheckSum"}));
    private final static String SEPARATOR = "/001";


    protected Map<String, Object> fixStrParts = new HashMap<>();

    //name, Field
    protected Map<String, Field> messageFields = new HashMap<>();
    //tag, name
    protected Map<Integer, String> tagName = new HashMap<>();

    protected Document document;

    public Map<String, Object> getFixStrParts() {
        return fixStrParts;
    }

    public Map<String, Field> getMessageFields() {
        return messageFields;
    }

    public String getFieldValueAsStringForTag(int tag){
        return fixStrParts.get(tagName.get(tag)).toString();
    }



    protected String result;
    public String getResult()
    {
        return result;
    }
    protected String type;
    public String getType(){
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    protected String msgName;
    public String getMsgName(){
        return msgName;
    }

    protected String fixStr;
    public String getFixStr()
    {
        return fixStr;
    }

    public Document getDocument() {return document;}

    public String getFixStrFromBody(String body){
        StringBuilder fixStr = new StringBuilder();
        fixStr.append(getHeader(body, type));
        fixStr.append(body);
        fixStr.append(getCheckSum(fixStr.toString()));
        return fixStr.toString();
    }

    //name, value
    //need messageFields    //copy from another Message
    public void getBodyFromIdenticalFixStrParts(Map<String, Object> parts){

        for (Map.Entry<String, Object> part: parts.entrySet()){
            String key = part.getKey();
            Object value = part.getValue();
            if (CONTINUEKEYS.contains(key)) continue;

            if (messageFields.containsKey(key))
                fixStrParts.put(key, value);
        }

    }

    //fixStrParts  (name, value) (only body parts)
    public String getFixStrFromParts(){
        try {

            StringBuilder sb = new StringBuilder();

            //name, Field
            for (Map.Entry<String, Field> field: messageFields.entrySet()){

                String key = field.getKey();
                Field value = field.getValue();
                int tag = value.getTag();

                if (BIGCONTINUEKEYS.contains(key)) continue;
                if (!fixStrParts.containsKey(key) && value.isRequired()) {
                    result = "required key " + tag + " not found";
                    return null;
                }

                if (fixStrParts.containsKey(key)){
                    sb.append(value.getTag());
                    sb.append("=");
                    sb.append(fixStrParts.get(key));
                    sb.append(SEPARATOR);
                }
            }

            StringBuilder st = new StringBuilder();
            st.append(getHeader(sb.toString(), type));
            st.append(sb.toString());
            st.append(getCheckSum(st.toString()));
            result = "ok";

            return st.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result = "Exception: " + e.getMessage();
            return null;
        }
    }

    public void addFixStrPart(int tag, String value){   //(need messageFields)
        addPart(fixStrParts, messageFields.get(tagName.get(tag)).getType(), value, tagName.get(tag));
    }

    protected String addPart(Map<String,Object> map, String type, String value, String name) {
        try {
            switch (type) {
                case ("java.lang.String"):
                    map.put(name, value);
                    break;

                case ("java.lang.Integer"):
                    map.put(name, Integer.parseInt(value));
                    break;

                case ("java.time.LocalDateTime"):
                    map.put(name, LocalDateTime.parse(value));

                    //String str = "2016-03-04 11:30:40";
                    /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                    map.put(name, dateTime);*/
                    break;

                default:
                    return "unknown type";
            }
            return "ok";
        }
        catch (Exception e)
        {return "Exception: " + e.getMessage();}
    }

    protected void setDocument(){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder  builder = factory.newDocumentBuilder();
            document = builder.parse(new File(FILENAME));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected String getHeader(String body, String type) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> header = new HashMap<>();

        sb.append("8=FIX.4.2");
        sb.append(SEPARATOR);

        sb.append("9=");
        Integer len = body.length();
        sb.append(len);
        sb.append(SEPARATOR);

        sb.append("35=");
        sb.append(type);
        sb.append(SEPARATOR);

        addPart(header,messageFields.get(tagName.get(8)).getType(),"FIX.4.2", tagName.get(8));
        addPart(header,messageFields.get(tagName.get(9)).getType(), len.toString(), tagName.get(9));
        addPart(header,messageFields.get(tagName.get(35)).getType(),type, tagName.get(35));
        fixStrParts.put("header", header);

        return sb.toString();
    }

    protected String getCheckSum(String str) {
        StringBuilder sb = new StringBuilder();

        sb.append("10=");

        int cs = str.length()%256;
        StringBuilder sum = new StringBuilder();
        sum.append(cs);
        while (sum.length()<3)
            sum.insert(0, "0");
        sb.append(sum);

        sb.append(SEPARATOR);
        fixStrParts.put("CheckSum", sb.toString());

        return sb.toString();
    }

    protected String setMessageFields(String check){
        try {
            NodeList messageElements = document.getDocumentElement().getElementsByTagName("message");
            for (int i = 0; i < messageElements.getLength(); i++) {
                Node message = messageElements.item(i);
                NamedNodeMap attributes = message.getAttributes();
                String msgtype = attributes.getNamedItem("msgtype").getNodeValue();
                if(check.equals(msgtype)){
                    msgName = attributes.getNamedItem("name").getNodeValue();
                    Element eElement = (Element) message;

                    NodeList mesFields = eElement.getElementsByTagName("field");
                    for (int j = 0; j < mesFields.getLength(); j++) {

                        Node fField = mesFields.item(j);
                        attributes = fField.getAttributes();

                        try {

                            String name = attributes.getNamedItem("name").getNodeValue();
                            String type = attributes.getNamedItem("type").getNodeValue();
                            boolean required = Boolean.parseBoolean(attributes.getNamedItem("required").getNodeValue());
                            int tag = Integer.parseInt(attributes.getNamedItem("tag").getNodeValue());
                            Field field = new Field(name, type, required, tag);
                            messageFields.put(name, field);       //put(tag, field) ?
                            tagName.put(tag, name);
                        }
                        catch (NumberFormatException e) {
                            return  "NumberFormatException";

                        }

                    }
                    return "ok";
                }
            }

            return "msgtype" + check + "not found";
        }
        catch (Exception e){
            //e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    public Message(String type){
        this.type = type;
        setDocument();
        result = setMessageFields(type);
    }

    public Message(){
        setDocument();
    }




    protected boolean checkingChecksum(String f, String fixStr)
    {
        try {
            if(f.length() != 3) return false;
            int control = Integer.parseInt(f);
            int itog = (fixStr.length()-6- SEPARATOR.length())%256;
            if (control == itog) return true;
            else return false;
        }
        catch (Exception e){
            //e.printStackTrace();
            return false;
        }
    }


    public void parseStrToMap(String fixStr){
        String[] parts = fixStr.split(SEPARATOR);
        String message;
        Map<Integer,String> allInOne = new HashMap<>();

        Map<String, Object> header = new HashMap<>();

        for (String part: parts)
        {
            try {
                String[] st = part.split("=");  //2 parts: key, value
                if(st.length != 2) {
                    result = "exception: " + st[0] + " have 2 characters '='";
                    return;
                }
                allInOne.put(Integer.parseInt(st[0]), st[1]);
            }
            catch (NumberFormatException e) {
                String[] st = part.split("=");
                result = "left part expression must be Integer (" + st[0] + ")";
                return;
            }
        }

        if (!checkingChecksum(allInOne.get(10), fixStr)) {
            result = "incorrect checksum";
            return;
        }


        if (type == null) type = allInOne.get(35);
        if (messageFields.isEmpty())
            if (!(result = setMessageFields(type)).equals("ok"))
                return;


        if (!allInOne.get(35).equals(type)) {
            result = "incorrect msgType";
            return;
        }


        List<Integer> headerKeys = new ArrayList<>(Arrays.asList(8,9,35));

        for(Map.Entry<String, Field> entry: messageFields.entrySet()) {
            //String key = entry.getKey();
            Field value = entry.getValue();
            Integer tag = value.getTag();

            //header
            if (headerKeys.contains(tag)) {
                if (!allInOne.containsKey(tag) && value.isRequired()) {
                    result = "required key " + tag + " not found";
                    return;
                }
                if (allInOne.containsKey(tag)){
                    if(!(message = addPart(header, value.getType(), allInOne.get(tag), value.getName())).equals("ok")) {
                        result = message;
                        return;
                    }
                    allInOne.remove(tag);
                    continue;
                }
            }

            if (!allInOne.containsKey(tag) && value.isRequired()) {
                result = "required key " + tag + " not found";
                return;
            }
            if (allInOne.containsKey(tag)){
                if(!(message = addPart(fixStrParts, value.getType(), allInOne.get(tag), value.getName())).equals("ok")) {
                    result = message;
                    return;
                }
                allInOne.remove(tag);
            }
        }

        fixStrParts.put("header", header);

        //extra keys
        if (allInOne.isEmpty()) {
            result = "ok";
        }
        else {
            result = "extra key(s): " + allInOne.keySet();
        }
    }
}

