import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.io.File;
import java.io.IOException;

public class Clean
{
    public static void main(String args[])
        throws IOException
    {
        Document doc = Jsoup.parse(new File(args[0]), "utf-8");

        Elements c2 = doc.select("p[class=c2]");

        for (Element el: c2) {
            System.out.println(clean(el.text()));
        }
    }

    private final static String clean(String in)
    {
        StringBuilder out = new StringBuilder();
        char[] c = in.toCharArray();
        for (int i=0; i<c.length; i++) {
            char cur = c[i];
            // "normal" ascii
            if ((cur == '\r') || (cur == '\n') ||
                ((cur >= ' ') && (cur <= '~'))) {
                out.append(cur);
                continue;
            }
            switch (cur) {
            case 160: break;
            case 8216: out.append('\''); break;
            case 8217: out.append('\''); break;
            case 8220: out.append('"'); break;
            case 8221: out.append('"'); break;
            case 232: out.append("e"); break;
            case 233: out.append("e"); break;
            case 178: out.append("^2"); break;
            case 8211: out.append(" - "); break;
            case 8212: out.append(" -- "); break;
            case 189: out.append(".5"); break;
            case 252: out.append("u"); break;
            case 231: out.append("c"); break;
            case 246: out.append("o"); break;
            case 223: out.append("ss"); break;
            case 225: out.append("a"); break;
            case 243: out.append("o"); break;
            case 237: out.append("i"); break;
            case 201: out.append("E"); break;
            case 228: out.append("a"); break;
            case 224: out.append("a"); break;
            case 163: out.append("#"); break;
            case 235: out.append("e"); break;

            default:
                System.err.println
                    ("tbd: "+(int)cur+" ("+in+")");
            }
        }
        return out.toString();
    }
}
