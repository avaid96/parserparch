//jetty shits
package org.eclipse.jetty.embedded;

import java.io.IOException;
import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

//xml shits
import org.w3c.dom.*;
import javax.xml.parsers.*;


public class HelloWorld extends AbstractHandler
{
    @Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
                                                      ServletException
    {
        if (target == "/") {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String payload = buffer.toString();
            // Declare response encoding and types
            response.setContentType("text/html; charset=utf-8");
            // Declare response status code
            response.setStatus(HttpServletResponse.SC_OK);

            Document doc;
            try {
                DocumentBuilderFactory factory = 
                DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                ByteArrayInputStream input =  new ByteArrayInputStream(payload.getBytes("UTF-8"));
                doc = builder.parse(input);
                ////////////////////////////////////////////
                Element parentMessage = doc.getDocumentElement();
                switch(parentMessage.getNodeName()) { //type of message received
                    case "start-game": 
                        System.out.println("starting game");
                        String color = parentMessage.getTextContent();
                        //!!!!!!!!!!!!!!!!!!!!
                        //call start game here 
                        //!!!!!!!!!!!!!!!!!!!!
                        String playerName = "jimmy";
                        // Write back response
                        response.getWriter().printf("<name>%s</name>", playerName);
                        break;
                    case "do-move":
                        System.out.println("doing move -------------------");
                        //Dice parsing 
                        System.out.println("Parsing dice");
                        int[] dieRolls = parseDie(doc);
                        System.out.println(dieRolls[0]);
                        System.out.println(dieRolls[1]);
                        //End of dice parsing 

                        //Board parsing
                        //make new board here
                        //starting area
                        System.out.println("Parsing start");
                        NodeList startL = doc.getElementsByTagName("start");
                        Node startN = startL.item(0); //there will be only one
                        Element start = (Element) startN;
                        NodeList startPawns = start.getElementsByTagName("pawn");
                        for (int i = 0; i < startPawns.getLength(); i++) {
                            System.out.println("parsing start pawn:"+i);
                            Element pawnI = (Element)startPawns.item(i);
                            parsePawn(pawnI);  //get a pawn from this and place it on a board
                        }

                        //main area
                        System.out.println("Parsing main");
                        NodeList mainL = doc.getElementsByTagName("main");
                        Node mainN = mainL.item(0); //there will be only one
                        Element main = (Element) mainN;
                        NodeList mpieceLocs = main.getElementsByTagName("piece-loc");
                        for (int i = 0; i < mpieceLocs.getLength(); i++) {
                            System.out.println("parsing piece loc:"+i);
                            Element plI = (Element)mpieceLocs.item(i);
                            parsePieceLoc(plI);
                        }

                        //home rows area
                        System.out.println("Parsing home rows");
                        NodeList hrL = doc.getElementsByTagName("home-rows");
                        Node hrN = hrL.item(0); //there will be only one
                        Element hr = (Element) hrN;
                        NodeList hpieceLocs = hr.getElementsByTagName("piece-loc");
                        for (int i = 0; i < hpieceLocs.getLength(); i++) {
                            System.out.println("parsing piece loc:"+i);
                            Element plI = (Element)hpieceLocs.item(i);
                            parsePieceLoc(plI);
                        }

                        //homes area
                        System.out.println("Parsing homes");
                        NodeList homeL = doc.getElementsByTagName("home");
                        Node homeN = homeL.item(0); //there will be only one
                        Element home = (Element) homeN;
                        NodeList homePawns = home.getElementsByTagName("pawn");
                        for (int i = 0; i < homePawns.getLength(); i++) {
                            System.out.println("parsing start pawn:"+i);
                            Element pawnI = (Element)homePawns.item(i);
                            parsePawn(pawnI);  //get a pawn from this and place it on a board
                        }

                        //End of board parsing 
                        System.out.println("end of move ------------------");
                        break;
                    case "doubles-penalty":
                        System.out.println("applying penal");
                        //!!!!!!!!!!!!!!!!!!!!
                        //Apply penalty
                        //!!!!!!!!!!!!!!!!!!!!
                        response.getWriter().println("<void></void>");
                        break;
                    default:
                        System.out.println("fake news");
                }
            } 
            catch(Exception e) {
                // xml parsing failed
                response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                response.getWriter().println("<h1>XML Parsing failed</h1>");
                System.out.println(e); //log the exception
            }
            
        }

        // Inform jetty that this request has now been handled
        baseRequest.setHandled(true);
    }
    
    public int[] parseDie(Document doc) {
        NodeList diceRolls = doc.getElementsByTagName("die");
        int roll1 = Integer.parseInt(diceRolls.item(0).getTextContent().trim());
        int roll2 = Integer.parseInt(diceRolls.item(1).getTextContent().trim());
        //???????????????????? Could there be multiple dice rolls?
        System.out.printf("Roll1:%d, Roll2:%d\n", roll1, roll2);
        int[] result = {roll1, roll2};
        return result;
    }

    //!!!!!!!!!!!! Make this return a pawn
    public void parsePawn(Element pawn) {
        Element colorE = (Element) pawn.getElementsByTagName("color").item(0);
        String color = colorE.getTextContent();
        Element idE = (Element) pawn.getElementsByTagName("id").item(0);
        int id = Integer.parseInt(idE.getTextContent().trim());
        System.out.printf("pawn with, color=%s and id=%d\n", color, id);
    }

    //!!!!!!!!!!!! Make this return a pawn and a location int
    public void parsePieceLoc(Element pl) {
        Element pawnE = (Element) pl.getElementsByTagName("pawn").item(0);
        parsePawn(pawnE);
        Element locE = (Element) pl.getElementsByTagName("loc").item(0);
        int loc = Integer.parseInt(locE.getTextContent().trim());
        System.out.printf("location=%d\n", loc);
    }
    
    public void print() {
        System.out.println("I am called by a network request");
    }

    public static void main( String[] args ) throws Exception
    {
        Server server = new Server(8000);
        server.setHandler(new HelloWorld());

        server.start();
        server.join();
    }
}
