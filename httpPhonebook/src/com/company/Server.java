package com.company;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.TreeSet;

public class Server implements Runnable {
    private Socket          s;
    private InputStream     is;
    private OutputStream    os;
    private Phonebook       pb;
    private String          errorMessage = "";
    private boolean         error        = false;

    public Server(Socket s, Phonebook pb) throws Throwable {
        this.pb = pb;
        this.s  = s;
        this.is = s.getInputStream();
        this.os = s.getOutputStream();
    }

    public void run() {
        try {
            LinkedList<String> request  = readInputHeaders();
            String             response = GetResponse(request);
            writeResponse(response);
        } catch (Throwable t) {
                /*do nothing*/
        } finally {
            try {
                s.close();
            } catch (Throwable t) {
                    /*do nothing*/
            }
        }
        System.err.println("Client processing finished");
    }

    private String GetResponse(LinkedList<String> request) {
        String response_prefix  =
                "<html><body>" +
                    "<tr><td><form action=\"command\">" +
                        "name:" +
                        "<input type=\"text\"   value=\"\"       name=\"NAME\"   />" +
                        "number:" +
                        "<input type=\"number\"   value=\"\"     name=\"NUMBER\" />" +
                        "<input type=\"submit\" value=\"show\"   name=\"SHOW\"   />" +
                        "<input type=\"submit\" value=\"add\"    name=\"ADD\"    />" +
                        "<input type=\"submit\" value=\"delete\" name=\"DELETE\" />";
        String response_suffix  =
                    "</form></td></tr>" +
                "</body></html>";
        String lineList         = "<table border=\"1\" style=\"width:50%\">";
        String filter           = "";

        if (request.size() > 0)
            filter = ExecuteCommand(request.get(0));

        TreeSet<Phonebook.Line> filteredList = pb.GetListOfNumbers(filter);
        if (filteredList != null)
            for (Phonebook.Line line : filteredList)
                lineList += "<tr><td>" + line.name + "</td><td>" + line.number + "</td></tr>";

        lineList += "</table>";

        return response_prefix + errorMessage + lineList + response_suffix;
    }

    private String ExecuteCommand(String initial) {
        String cmd[]  = initial.split("\\s");

        if (cmd.length != 3) {
            error = true;
            System.err.print("COMMAND ERROR");
            return "";
        }

        String name   = ParseName(cmd[1]);
        String number = ParseNumber(cmd[1]);

        if (cmd[1].contains("DELETE")) {
            if (!pb.DeleteLine(name, number)) {
                errorMessage = "<td><strong>DELETE: LINE NOT EXISTS ERROR</strong></td>";
                System.err.print("DELETE: LINE NOT EXISTS ERROR");
            }
        } else if (cmd[1].contains("ADD")) {
            if (number.length() == 0 || name.length() == 0 || !pb.AddLine(name, number)) {
                errorMessage = "<td><strong>ADD: LINE EXISTS ERROR</strong></td>";
                System.err.print("ADD: LINE EXISTS ERROR");
            }
        } else if (cmd[1].contains("SHOW")) {
                return name;
        } else
                errorMessage = "<td><strong>incorrect url arguments</strong></td>";

        return "";
    }

    private String ParseName(String s) {
        int nameIndex   = s.indexOf("NAME=");

        if (nameIndex < 0)
            return "";

        nameIndex   += 5;
        int ampersandIndex = s.indexOf("&", nameIndex);

        return s.substring(nameIndex, ampersandIndex);
    }

    private String ParseNumber(String s) {
        int numberIndex = s.indexOf("NUMBER=");

        if (numberIndex < 0)
            return "";

        numberIndex += 7;
        int ampersandIndex = s.indexOf("&", numberIndex);

        return s.substring(numberIndex, ampersandIndex);
    }

    private void writeResponse(String s) throws Throwable {
        String response;
        if (error)
            response = "400 Bad Request";
        else
            response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
        String result = response + s;
        os.write(result.getBytes());
        os.flush();
    }

    private LinkedList<String> readInputHeaders() throws Throwable {
        LinkedList<String> request = new LinkedList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while (true) {
            String s = br.readLine();
            if (s == null || s.trim().length() == 0) {
                break;
            }
            request.add(s);
        }

        return request;
    }
}
