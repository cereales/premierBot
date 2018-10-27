package fr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Html {
    private String keyword;
    private ArrayList<String> definitions;

    public Html(String keyword) {
        definitions = new ArrayList();
        this.keyword = keyword;
        try {
            System.out.println("definition from https://www.larousse.fr/dictionnaires/francais/" + keyword);
            String htmlResponse = get("https://www.larousse.fr/dictionnaires/francais/" + keyword);

            int definitionsIndex = htmlResponse.indexOf("<ul class=\"Definitions\">");
            if (definitionsIndex == -1)
                return;

            int definitionsIndexEnd = htmlResponse.indexOf("</ul>", definitionsIndex) + 5;
            htmlResponse = htmlResponse.substring(definitionsIndex, definitionsIndexEnd);

            int definitionIndex = htmlResponse.indexOf("<li");
            while (definitionIndex >= 0) {
                definitionIndex = htmlResponse.indexOf(">", definitionIndex) + 1;
                definitionsIndexEnd = htmlResponse.indexOf("</li>", definitionIndex);
                definitions.add(definition(htmlResponse.substring(definitionIndex, definitionsIndexEnd)));
                definitionIndex = htmlResponse.indexOf("<li", definitionsIndexEnd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String definition(String def) {
        System.out.println("def of  " + def);
        def = def.replaceAll("&nbsp;", " ");
        def = layoutBaliseDefinition(def, "Exemple");
        def = layoutBaliseDefinition(def, "Remarque");
        def = layoutBaliseDefinition(def, "indicateur");
        def = retirerBalises(def);
        return def;
    }

    private String retirerBalises(String def) {
        int startIndex, startOpenIndex, endOpenIndex, startCloseIndex, endCloseIndex;
        String label;
        String res = "";

        startIndex = 0;
        startOpenIndex = def.indexOf("<");
        if (startOpenIndex == -1)
            return def;
        endOpenIndex = def.indexOf(">", startOpenIndex);
        if (endOpenIndex == -1)
            return "<" + retirerBalises(def.substring(1));
        label = def.substring(startOpenIndex + 1, endOpenIndex).split(" ")[0];
        startCloseIndex = def.indexOf("</" + label + ">", endOpenIndex);
        endCloseIndex = startCloseIndex + 2 + label.length();

        return def.substring(0, startOpenIndex)
            + retirerBalises(def.substring(endOpenIndex + 1, startCloseIndex))
            + retirerBalises(def.substring(endCloseIndex  +1));
    }

    private String layoutBaliseDefinition(String def, String label) {
        int definitionsIndex = def.indexOf("<span class=\"" + label + "Definition\">");
        int definitionsIndexEnd = def.indexOf("</span>", definitionsIndex);
        if (definitionsIndex >= 0) {
            if (String.valueOf(def.charAt(definitionsIndexEnd - 1)).equals(" "))
                return def.substring(0, definitionsIndex)
                    + "*" + def.substring(definitionsIndex + 25 + label.length(), definitionsIndexEnd - 1)
                    + "* " + def.substring(definitionsIndexEnd + 7);
            return def.substring(0, definitionsIndex)
                + "*" + def.substring(definitionsIndex + 25 + label.length(), definitionsIndexEnd)
                + "*" + def.substring(definitionsIndexEnd + 7);
        }
        return def;
    }

    public static String get(String url) throws IOException {

        String source ="";
        URL oracle = new URL(url);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(
                yc.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            source +=inputLine;
        in.close();
        return source;
    }

    @Override
    public String toString() {
        if (!hasDef())
            return "**" + keyword + "** :\nPas de définition disponible ...";
        String res = "**" + keyword + "** :";
        for (String def: definitions) {
            res += "\n- " + def;
        }
        return res;
    }

    public boolean hasDef() {
        return definitions.size() != 0;
    }
}
