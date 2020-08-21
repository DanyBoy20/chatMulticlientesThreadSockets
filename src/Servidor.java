
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

// CLASE PRINCIPAL, EXTIENDE A LA CLASE HILO, PUESTO QUE SE ABRIRAN VARIOS SEGUN LOS CLIENTES CONECTADOS
public class Servidor extends Thread {
    
    // atributos de la clasepara el socket del servidor, el puerto a que se conectara
    // guardar el nombre de quien se conecta y los stream de entrada y salida para los mensajes que se envien y reciban
    // tambien en un arraylist guardo la lista de clientes que se conectan
    private static ArrayList<BufferedWriter> clientes;
    private static ServerSocket servidor;
    private String nombre;
    private Socket puerto;
    private InputStream entrada;
    private InputStreamReader lectorEntrada;
    private BufferedReader buferLectura;
    
    // el constructor de la clase, de parametro el puerto que se abrira
    // en un tru catch asigno los stream de lectura al puerto previamente indicado.
    public Servidor(Socket puerto) {
        this.puerto = puerto;
        try {
            entrada = puerto.getInputStream();
            lectorEntrada = new InputStreamReader(entrada);
            buferLectura = new BufferedReader(lectorEntrada);
        } catch (IOException e) {
        }
    }
    
    // METODO RUN : Recordemos que el metodo RUN es para los hilos. ya extendimos la clase principal Servidor a Thread
    @Override
    public void run() {
        // en un try catch asigno los stream de lectura y escritura al puerto predeterminado
        // y asigno a la lista clientes el estream de escritura de la memoria temporal (bufer de escritura)
        try {
            String mensaje;
            OutputStream salida = this.puerto.getOutputStream();
            Writer escritura = new OutputStreamWriter(salida);
            BufferedWriter buferEscritura = new BufferedWriter(escritura);
            clientes.add(buferEscritura);
            nombre = mensaje = buferLectura.readLine(); // leo el mensaje del bufer segun el clinete conectado
            // mientras no se escriba la palabra "Salir" o el mensaje enviado sea diferente de nulo
            while (!"Salir".equalsIgnoreCase(mensaje) && mensaje != null) {
                mensaje = buferLectura.readLine(); // asigno a mensaje lo que se haya escrito (guardado en el bufer)
                // llamamos al metodo enviarmensajes, lo que hace es enviar los mensajes a todas las ventanas 
                // de los clientes que esten conectados
                enviarMensajes(buferEscritura, mensaje); 
                // y en la ventana del servidor, que es la consola
                // se escribiran tambien los mensajes que se vayan escribiendo
                System.out.println(mensaje); 
                
            }
        } catch (IOException e) {
        }
    }
    
    // METODO PARA ENVIAR LOS MENSAJES A TODAS LAS VENTANAS
    public void enviarMensajes(BufferedWriter escribir, String mensajeEscrito) throws IOException {
        BufferedWriter escrito;
        // en un ciclo for, voy escribiendo los mensajes acorde a los clientes
        for (BufferedWriter buferEscrito : clientes) {
            escrito = (BufferedWriter) buferEscrito;
            if (!(escribir == escrito)) { // si los mensajes son diferentes, es decir, el mensaje en el bufer y mensaje escrito
                // mando a escribir en pantallas quien escribe el mensaje y su mensaje
                buferEscrito.write(nombre + " -> " + mensajeEscrito + "\r\n"); 
                buferEscrito.flush(); // limpio el bufer para otros mensajes
            }
        }
    }  

    // METODO PRINCIPAL QUE EJECUTA EL PROGRAMA DEL SERVIDOR
    public static void main(String[] args) {
        // en un try catch creare el socket con los valores descritos (numero de puerto)
        // en un cuadro de mensaje establezco el valor del puerto por defecto, es posible cambiarlo
        try {
            JLabel lblTitulo = new JLabel("Puerto en el servidor:");
            JTextField txtPuerto = new JTextField("1201");
            Object[] texts = {lblTitulo, txtPuerto};
            JOptionPane.showMessageDialog(null, texts);
            // creo el socket con el puerto por parametro
            servidor = new ServerSocket(Integer.parseInt(txtPuerto.getText()));
            clientes = new ArrayList<>(); // creo un arraylst para los clientes que se conecten
            //cadena original:  clientes = new ArrayList<BufferedWriter>();
            // mensaje que me muestra el puerto activo
            JOptionPane.showMessageDialog(null, "Servidor activo en el puerto: " + txtPuerto.getText());
            //  mientras la condicion se cumpla
            while (true) {
                System.out.println("Esperando conexion ..."); // mensaje en consola
                Socket puertoDeConexion = servidor.accept(); // acepto (abro) las conexion (puerto) para que se conecten clientes
                System.out.println("Cliente conectado ..."); // mensaje cuando un cliente se conecta (mensaje en consola)
                Thread t = new Servidor(puertoDeConexion); // creo un hilo para esa conexion (el cliente que se conecta)
                t.start(); // inicio el hilo
            }
        } catch (HeadlessException | IOException | NumberFormatException e) {
        }
    } 
    
}
