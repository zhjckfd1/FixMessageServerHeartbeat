package com.mes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class TestMessage {
    public static void main(String[] args) {

        try {
            String str1 = "11=123123/00141=BROKER11/001581=12/00164=z/001";
            //String str1 = "8=FIX.4.2/0019=178/00135=8/00111=123123/00141=BROKER11/001581=12/00164=z/00110=128/001";
            System.out.println("1:");
            Message m1 = new Message("8");
            //m1.setMessageFields(m1.getType());
            //System.out.println(m1.getType());
            String fixStr = m1.getFixStrFromBody(str1);
            m1.parseStrToMap(fixStr);
            System.out.println(m1.getResult());


            String str2 = "11=123123/001";
            //String str2 = "8=FIX.4.2/0019=178/00135=8/00111=123123/00110=128/001";
            System.out.println("2:");
            Message m2 = new Message("8");
            //m2.setMessageFields(m2.getType());
            fixStr = m2.getFixStrFromBody(str2);
            m2.parseStrToMap(fixStr);
            System.out.println(m2.getResult());
            Map<String, Object> map2 = m2.getFixStrParts();
            System.out.println(map2);
            //System.out.println(m2.parseMapToStr());


            String str3 = "11=123123/00141=BROKER11/001583=12/001";
            //String str3 = "8=FIX.4.2/0019=178/00135=8/00111=123123/00141=BROKER11/001583=12/00110=128/001";
            System.out.println("3:");
            Message m3 = new Message("8");
            //m3.setMessageFields(m3.getType());
            fixStr = m3.getFixStrFromBody(str3);
            m3.parseStrToMap(fixStr);
            System.out.println(m3.getResult());
            Map<String, Object> map3 = m3.getFixStrParts();
            System.out.println(map3);
            //System.out.println(m3.parseMapToStr());


            String str4 = "11=123123/00141=BROKER11/001581=12/00164=z/001";
            //String str4 = "8=FIX.4.2/0019=178/00135=8/00111=123123/00141=BROKER11/001581=12/00164=z/00110=123/001";
            System.out.println("4:");
            Message m4 = new Message("8");
            //m4.setMessageFields(m4.getType());
            fixStr = m4.getFixStrFromBody(str4);
            m4.parseStrToMap(fixStr);
            System.out.println(m4.getResult());
            //System.out.println(m4.parseMapToStr());


            String str5 = "11=123123/00160=2021-02-15T12:00:00.640/001583=12/001";
            //String str5 = "8=FIX.4.2/0019=178/00135=D/00111=123123/00160=2021-02-15T12:00:00.640/001583=12/00110=128/001";
            System.out.println("5:");
            LocalDateTime l = LocalDateTime.now();
            System.out.println(l);
            Message m5 = new Message("D");
            //m5.setMessageFields(m5.getType());
            fixStr = m5.getFixStrFromBody(str5);
            m5.parseStrToMap(fixStr);
            System.out.println(m5.getResult());
            Map<String, Object> map5 = m5.getFixStrParts();
            System.out.println(map5);
            System.out.println( m5.getFixStrFromParts());
            System.out.println();


            ArrayList<String> filePath = new ArrayList<>(Arrays.asList("resources/fix1.txt", "resources/fix2.txt"));
            int z = (int) (Math.random()*filePath.size());
            System.out.println(filePath.get(z));   //send


            String str6 = "8=FIX.4.2/0019=53/00135=D/00111=123123/001583=12/00160=2021-02-15T12:00:00.640/00110=082/001";
            Message m6 = new Message();
            //System.out.println(m6.getType());
            m6.parseStrToMap(str6);
            System.out.println(m6.getType());
            System.out.println(m6.getResult());
            Map<String, Object> map6 = m6.getFixStrParts();
            System.out.println(map6);
            System.out.println( m6.getFixStrFromParts());
            System.out.println();



            // 11=123123/00160=2021-02-15T12:00:00.640/001583=12/001
            // 11=123123/00141=BROKER11/001581=12/00164=z/001
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

