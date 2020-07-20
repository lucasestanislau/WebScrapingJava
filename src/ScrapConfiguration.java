import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author lucas
 */
public class ScrapConfiguration {
    private String linkScrap;//atributo que conterá o link do site para o scrap
    private OutputStreamWriter bOut; //atributo responsável pelo encapsulamento do arquivo de gravação
    private String path = "D:\\FACULDADE\\semestre_5\\topicos_avancados\\webScraping.csv";//caminho onde será salvo o arquivo csv

    public ScrapConfiguration() {
        configFile();
        run(bOut);
    }
    /*método responsável pela interação do link com datas diferentes
        Utilizando o calendário gregoriano*/
    public void run(OutputStreamWriter bufferOut) {
        GregorianCalendar calendar = new GregorianCalendar();
        SimpleDateFormat dia = new SimpleDateFormat("dd");
        SimpleDateFormat mes = new SimpleDateFormat("MM");
        SimpleDateFormat ano = new SimpleDateFormat("yyyy");
        while (true) {
            System.out.println(dia.format(calendar.getTime()) + "/" + mes.format(calendar.getTime()) + "/" + ano.format(calendar.getTime()));
            formatarLink(dia.format(calendar.getTime()), mes.format(calendar.getTime()), ano.format(calendar.getTime()));
            searchAtrr(calendar, bufferOut);
            calendar.add(GregorianCalendar.DATE, -1);//para cada interação, a data é decrementada, assim o programa ficará executando até ser parado
        }
    }

    /*método responsável pelas configurações iniciais do arquivo de gravação*/
    private void configFile() {
        try {
            File arquivo = new File(this.path);
            if (!arquivo.exists()) {
                arquivo.createNewFile();
            }
            FileOutputStream fo = new FileOutputStream(arquivo);
            bOut = new OutputStreamWriter(fo, "UTF-8");
            bOut.write("ZONA\tNeighborhood\tStreet\tStart date and time\tEnd date and time\tDirection\tReference\tStatus\n");
            bOut.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /*método que será responsável por chamar o método search da Classe responsável por iniciar a coneção com o site e
        realizar a estração dos atributos necessários para o scraping*/
    public void searchAtrr(Calendar calendar, OutputStreamWriter bufferOut) {
        Scrap.search(calendar, this.linkScrap, bufferOut);
    }
    /*método responsável por formatar o link do scrap de acordo com o dia, mês e ano solicitado*/
    public void formatarLink(String dia, String mes, String ano) {
        this.linkScrap = "https://www.cgesp.org/v3/alagamentos.jsp?dataBusca=" + dia + "%2F" + mes + "%2F" + ano + "&enviaBusca=Buscar";
    }
}
