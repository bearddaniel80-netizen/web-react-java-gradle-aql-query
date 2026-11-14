package example.model;

public record Column(String key, String label, String type) {

    /** Python-style str.title(): capitalise the first letter after any non-letter. */
    public static String title(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean startOfWord = true;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(startOfWord ? Character.toUpperCase(c) : Character.toLowerCase(c));
                startOfWord = false;
            } else {
                sb.append(c);
                startOfWord = true;
            }
        }
        return sb.toString();
    }

    public static Column of(String key, String type) {
        return new Column(key, title(key), type);
    }
}
