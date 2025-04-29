import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubActivityCLI {
    public static void main(String[] args) {

        if(args.length != 1) {
            System.out.println("Usage: java GitHubActivityCLI <github-username>");
            return;
        }

        String username = args[0];
        fetchGitHubActivity(username);
    }

    private static void fetchGitHubActivity(String username) {
        String apiUrl = "https://api.github.com/users/" + username + "/events";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();

            if(responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                }

                in.close();
                parseAndDisplayActivity(content.toString(), username);
            }else if(responseCode == 404) {
                System.out.println("Erreur: utilisateur " + username + " non existant.");
            }else {
                System.out.println("Erreur: Impossible de charger les données. ");
            }

        }catch (Exception e) {
            System.out.println("Erreur système réseau: "+e.getMessage());
        }
    }

    private static void parseAndDisplayActivity(String jsonResponse, String username) {
        String[] events = jsonResponse.split("\\},\\{"); // Séparer les objets JSON
        for (String event : events) {
            String type = extractValue(event, "\"type\":\"", "\"");
            String repo = extractRepoName(event).replace(username+"/", "");
            String action = "";

            switch (type) {
                case "PushEvent":
                    String commits = extractValue(event, "\"size\":", ",");
                    action = username+ " Pushed " + commits + " commits to " + repo;
                    break;
                case "IssuesEvent":
                    String issueAction = extractValue(event, "\"action\":\"", "\"");
                    action = username+ " " +capitalize(issueAction) + " an issue in " + repo;
                    break;
                case "WatchEvent":
                    action = username+ " Starred " + repo;
                    break;
                default:
                    action = username+ " " + type + " in " + repo;
                    break;
            }

            System.out.println("- " + action);
        }
    }

    private static String extractRepoName(String event) {
        String repoSection = extractValue(event, "\"repo\":{", "}");
        return extractValue(repoSection, "\"name\":\"", "\"");
    }

    private static String extractValue(String source, String startDelimiter, String endDelimiter) {
        int start = source.indexOf(startDelimiter);
        if (start == -1) return "";
        start += startDelimiter.length();
        int end = source.indexOf(endDelimiter, start);
        if (end == -1) return "";
        return source.substring(start, end);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


}