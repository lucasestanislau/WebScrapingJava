import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author lucas
 */
public class Scrap {

    static SimpleDateFormat data = new SimpleDateFormat("yyyy/MM/dd");//formato da data
    static ArrayList<Integer> poxisZonas;//aray que conterá as posições das zonas dentro do documento
    static Elements divContent;//conteúdo que está dentro da <div class='content'>

    /*Este método será responsável por preencher as posições do array poxisZonas
     com as posições das zonas no Document doc.
     Funcionamento: a variável divContent receberá todos os elementos de doc,
     a variável zonasEncontradas receberá os elementos que contém a class tit-bairros (no caso, as zonas)
     para cada elemento da div que seja equivalente ao elemento da zona, adiciono a posição dele no poxisZonas*/
    private static void configIndexZonas(Document doc) {
        divContent = doc.getAllElements();
        Elements zonasEncontradas = doc.getElementsByClass("tit-bairros");
        int countZona = 0;
        poxisZonas = new ArrayList<>();
        for (Element x : divContent) {
            if (countZona < zonasEncontradas.size() && x.text().equals(zonasEncontradas.get(countZona).text())) {
                poxisZonas.add(divContent.indexOf(x));
                countZona++;
            }
        }
    }

    /*este método é responsável por procurar os elementos necessários para o Scrap*/
    public static void search(Calendar calendar, String link, OutputStreamWriter bufferOut) {
        Document doc = null;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException ex) {
            System.out.println("Site indisponível ou falha na requisição!");
        }
        configIndexZonas(doc);
        for (Element neighbor : doc.getElementsByClass("tb-pontos-de-alagamentos")) {
            Elements neighborName = neighbor.getElementsByClass("bairro arial-bairros-alag linha-pontilhada");
            for (Element floodPoints : neighbor.getElementsByClass("ponto-de-alagamento")) {

                CgespSp cgesp = new CgespSp();
                cgesp.setZona(findZona(neighbor));//encontra a zona enviando um elemento como referência de index
                cgesp.setNeighborhood(neighborName.text());
                Elements li = floodPoints.select("li");
                cgesp.setStatus(li.get(0).attr("title"));
                String horarioInicio = startDataFormat(li.get(2).text());
                String horarioFim = endDataFormat(li.get(2).text());
                cgesp.setStartDate(formatHourI(horarioInicio, calendar));
                cgesp.setEndDate(formatHourF(horarioInicio, horarioFim, calendar));
                cgesp.setStreet(streetFormat(li.get(2).text()));
                cgesp.setDirection(directionFormat(li.get(4).text()));
                cgesp.setReference(referenceFormat(li.get(4).text()));
                SalvarDado.salvarEventoAlagamento(cgesp, bufferOut);
                exibirDadosConsole(cgesp);
            }
        }
    }

    /*esse método será responsável por procurar a zona que determinado elemento pertence, de acordo com o seu índice
     Retorna uma String com o nome da zona*/
    private static String findZona(Element element) {
        int index = -1;
        for (Element a : divContent) {
            if (a.text().equals(element.text())) {
                index = divContent.indexOf(a);
                break;
            }
        }
        for (int i = 0; i < poxisZonas.size() - 1; i++) {
            if (index > poxisZonas.get(i) && index < poxisZonas.get(i + 1)) {
                return divContent.get(poxisZonas.get(i)).text();
            }
        }
        return divContent.get(poxisZonas.get(poxisZonas.size() - 1)).text();
    }

    /*método responsável por exibir os dados coletados no console*/
    private static void exibirDadosConsole(CgespSp cgesp) {

        System.out.println("Zona: " + cgesp.getZona());
        System.out.println("Neighborhood: " + cgesp.getNeighborhood());
        System.out.println("START: " + cgesp.getStartDate());
        System.out.println("END: " + cgesp.getEndDate());
        System.out.println("Reference: " + cgesp.getReference());
        System.out.println("Direction: " + cgesp.getDirection());
        System.out.println("STREET: " + cgesp.getStreet());
        System.out.println("STATUS: " + cgesp.getStatus());
        System.out.println("***********************************************");;
    }

    /*método responsável por formatar a data e horário do fim do evento de alagamento
     Retorna uma String com a data e hora formatada*/
    private static String formatHourF(String inicio, String fim, Calendar calendar) {
        if (fim.contains("FALHOU")) {
            return "Horário não informado no Site";
        }
        String[] ini = inicio.split(":");
        String[] fi = fim.split(":");
        if (Integer.parseInt(ini[0]) < Integer.parseInt(fi[0])) {
            return Scrap.data.format(calendar.getTime()) + " "+fim;
        } else if (Integer.parseInt(ini[0]) == Integer.parseInt(fi[0]) && Integer.parseInt(ini[1]) <= Integer.parseInt(fi[1])) {
            return Scrap.data.format(calendar.getTime()) + " "+fim;
        } else {
            calendar.add(GregorianCalendar.DATE, 1);
            String result = Scrap.data.format(calendar.getTime()) + " "+fim;
            calendar.add(GregorianCalendar.DATE, -1);
            return result;
        }
    }

    /*método responsável por formatar a data e horario do início do evento de alagamento
     Retorna uma String com a data e hora formatada*/
    private static String formatHourI(String hora, Calendar calendar) {
        return Scrap.data.format(calendar.getTime())+" "+hora;
    }

    /*método responsável por extrair e formatar a data ínicio vinda do texto do elemento*/
    private static String startDataFormat(String text) {
        String[] horarios = text.split("a");
        String start = horarios[0];
        Pattern pattern = Pattern.compile("\\d\\d:\\d\\d");
        Matcher mat = pattern.matcher(start);
        if (mat.find()) {
            return mat.group();
        } else {
            return "PADRÃO DE HORA INICIAL FALHOU!";
        }
    }

    /*método responsável por extrair e formatar a data fim vinda do texto do elemento*/
    private static String endDataFormat(String text) {
        String[] horarios = text.split("a");
        String end = horarios[1];
        Pattern pattern = Pattern.compile("\\d\\d:\\d\\d");
        Matcher mat = pattern.matcher(end);
        if (mat.find()) {
            return mat.group();
        } else {
            return "PADRÃO DE HORA FINAL FALHOU!";
        }
    }
    
    /*método responsável por extrair e formatar a rua vinda do texto do elemento*/
    private static String streetFormat(String text) {
        String[] spl = text.split("a");
        Pattern pattern = Pattern.compile("\\d\\d:\\d\\d");
        Matcher mat = pattern.matcher(spl[1]);
        if (mat.find()) {
            return spl[1].replace(mat.group(), "").trim();
        } else {
            return "Padrão não compreendido";
        }
    }
    
    /*método responsável por extrair e formatar a direção vinda do texto do elemento*/
    private static String directionFormat(String text) {
        String[] spl = text.split("Referência:");
        String[] spl2 = spl[0].split("entido:");
        return spl2[1].trim();

    }
    
    /*método responsável por extrair e formatar a referência vinda do texto do elemento*/
    private static String referenceFormat(String text) {
        String[] spl = text.split("Referência:");
        try {
            return spl[1].trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Referência não informada no site";
        }
    }

}
