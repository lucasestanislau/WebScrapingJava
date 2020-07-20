import java.io.IOException;
import java.io.OutputStreamWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author lucas
 */
public class SalvarDado {
    /*método que salvará o evento de alagamento no arquivo csv
    parâmetros: dados (representa os dados do evento) e bufferOut (Representa o arquivo onde será gravado os dados)*/
    public static void salvarEventoAlagamento(CgespSp dados, OutputStreamWriter bufferOut) {
        try {
            bufferOut.write(dados.getZona() +"\t" +dados.getNeighborhood() + "\t" + dados.getStreet() + "\t" + dados.getStartDate() + "\t"
                    + dados.getEndDate() + "\t" + dados.getDirection() + "\t" + dados.getReference() + "\t"
                    + dados.getStatus() + "\n");
            bufferOut.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
