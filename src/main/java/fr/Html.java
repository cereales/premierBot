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
            String htmlResponse = get("https://www.larousse.fr/dictionnaires/francais/" + keyword);
            int definitionsIndex = htmlResponse.indexOf("<ul class=\"Definitions\">");
            int definitionsIndexEnd = htmlResponse.indexOf("</ul>", definitionsIndex) + 5;
            if (definitionsIndex > 0)
                htmlResponse = htmlResponse.substring(definitionsIndex, definitionsIndexEnd);

            definitionsIndex = htmlResponse.indexOf("<li");
            while (definitionsIndex >= 0) {
                definitionsIndex = htmlResponse.indexOf(">", definitionsIndex) + 1;
                definitionsIndexEnd = htmlResponse.indexOf("</li>", definitionsIndex);
                definitions.add(definition(htmlResponse.substring(definitionsIndex, definitionsIndexEnd)));
                definitionsIndex = htmlResponse.indexOf("<li", definitionsIndexEnd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String definition(String def) {
        def = def.replaceAll("&nbsp;", " ");
        def = layoutExempleDefinition(def);
        return def;
    }

    private String layoutExempleDefinition(String def) {
        int definitionsIndex = def.indexOf("<span class=\"ExempleDefinition\">");
        int definitionsIndexEnd = def.indexOf("</span>", definitionsIndex);
        if (definitionsIndex > 0)
            return def.substring(0, definitionsIndex)
                + "*" + def.substring(definitionsIndex + 32, definitionsIndexEnd)
                + "*" + def.substring(definitionsIndexEnd + 7);
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
        if (definitions.size() == 0)
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
