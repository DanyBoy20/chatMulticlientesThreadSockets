
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

// CLASE PRINCIPAL PARA EL CLIENTE, EXTIENDE A JFRAME POR QUE LA INTERFAZ SERA EN UN FRAME E IMPLEMENTE 
// CLASES PARA LAS ACCIONES DE LOS BOTONES CUANDO SEAN PRESIONADOS O CUANDO SE PRESIONE LA TECLA ENTER
public class Cliente extends JFrame implements ActionListener, KeyListener  {
    
    // constantes (variables finales) para elementos del formulario
    private final JTextField txtDireccionIP;
    private final JTextField txtPuerto;
    private final JTextField txtNombreCliente;
    private final JTextArea text;
    private final JTextField txtMensaje;
    private final JButton btnEnviar;
    private final JButton btnSalir;
    private final JLabel lblMensajesEnChat;
    private final JLabel lblMensaje;
    private final JPanel jpnlContenidoVentana;
    // variables para el socket de lado cliente y los streams (para los mensajes que se envien
    private Socket socket;
    private OutputStream streamEscritura;
    private Writer escribir;
    private BufferedWriter buferEscritura; 

    // EM CONSTRUCTOR DE LA CLASE
    public Cliente() throws IOException {
        // los atributos de la clase, con sus valores por defecto (elementos de formulario con valores por defecto
        JLabel lblMessage = new JLabel("Validar datos");
        txtDireccionIP = new JTextField("127.0.0.1");
        txtPuerto = new JTextField("1201");
        txtNombreCliente = new JTextField("Cliente");
        Object[] texts = {lblMessage, txtDireccionIP, txtPuerto, txtNombreCliente};
        JOptionPane.showMessageDialog(null, texts); // mensaje que me permite mostrar los valores por defecto o cambiarlos
        // atributos de la clase, que son los elementos del formulario y sus valores: ubicacion tamaño, titulos, etc.
        jpnlContenidoVentana = new JPanel();
        text = new JTextArea(10, 20);
        text.setEditable(false);
        text.setBackground(new Color(255, 255, 255));
        txtMensaje = new JTextField(20);
        lblMensajesEnChat = new JLabel("Mensajes enviados");
        lblMensaje = new JLabel("Mensaje");
        btnEnviar = new JButton("Enviar");
        btnEnviar.setToolTipText("Enviar mensaje");
        btnSalir = new JButton("Salir");
        btnSalir.setToolTipText("Salir del chat");
        btnEnviar.addActionListener(this);
        btnSalir.addActionListener(this);
        btnEnviar.addKeyListener(this);
        txtMensaje.addKeyListener(this);
        JScrollPane scroll = new JScrollPane(text);
        text.setLineWrap(true);
        jpnlContenidoVentana.add(lblMensajesEnChat);
        jpnlContenidoVentana.add(scroll);
        jpnlContenidoVentana.add(lblMensaje);
        jpnlContenidoVentana.add(txtMensaje);
        jpnlContenidoVentana.add(btnSalir);
        jpnlContenidoVentana.add(btnEnviar);
        setTitle(txtNombreCliente.getText());
        setContentPane(jpnlContenidoVentana);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(250, 300);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    // METODO PARA REALIZAR LA CONEXION AL PUERTO (CREADO Y ABIERTO EN EL SOCKET)
    public void conectarse() throws IOException {
        // creo un socket y por parametro la direccion del servidor y puerto a conectar
        // y con stream de escritura del socket, a la salida (outputstreamwriter y buferwriter)
        // el nombre del cliente conectado, esto es para que el servidor los identifique
        socket = new Socket(txtDireccionIP.getText(), Integer.parseInt(txtPuerto.getText()));
        streamEscritura = socket.getOutputStream();
        escribir = new OutputStreamWriter(streamEscritura);
        buferEscritura = new BufferedWriter(escribir);
        buferEscritura.write(txtNombreCliente.getText() + "\r\n");
        buferEscritura.flush(); // borro el bufer de escritura cuando ya se envio el dato del cliente al servidor
    }
    
    // METODOS PARA ENVIAR MENSAJES DE LOS CLIENTES
    public void enviarMensaje(String mensaje) throws IOException {
        if (mensaje.equals("Salir")) { // si se escribe la palabra salir
            buferEscritura.write("Desconectado \r\n"); // se escribira (a traves del bufer) la palabra desconectado
            text.append("Desconnected \r\n"); // y añado desconectado al area de texto 
        } else { // si no se cumple la condicion anterior
            buferEscritura.write(mensaje + "\r\n"); // envio el mensaje que se escribio al bufer de escritura (bufferedwriter)
            // y lo añado a la ventana de mensajes (area de texto) y le doy formato de salida
            text.append(txtNombreCliente.getText() + " : " + txtMensaje.getText() + "\r\n");
        }
        buferEscritura.flush(); // limpio el bufer de escritura para otros mensajes
        txtMensaje.setText(""); // limpio mi caja de texto
    }
    
    // METODO PARA LEER LOS MENSAJES  
    public void leer() throws IOException {
        // creo los stream de lectura del socket y los asigno a los stream creados (reader y bufer)
        InputStream lectura = socket.getInputStream();
        InputStreamReader streamDeLectura = new InputStreamReader(lectura);
        BufferedReader buferDeLectura = new BufferedReader(streamDeLectura);
        String mensaje = ""; // variable para guardar los mensajes
        // mientrras que no escriban la palabra salir
        while (!"Salir".equalsIgnoreCase(mensaje)) {
            if (buferDeLectura.ready()) { // si el bufer esta listo para leer
                mensaje = buferDeLectura.readLine(); // lee y lo asigna a la variable mensaje
                if (mensaje.equals("Salir")) { // si el mensaje escrito fue salir
                    text.append("Servidor fuera de linea \r\n"); // añado un mensale al area de texto de salida del servidor
                } else { // sino
                    text.append(mensaje + "\r\n"); // añado el mensaje escrito
                }
            }
        }
    }
    
    // METODO PARA SALIR (O CERRAR) DEL SOCKET Y LOS STREAMS ABIERTOS
    public void sair() throws IOException {
        enviarMensaje("Salir");
        buferEscritura.close();
        escribir.close();
        streamEscritura.close();
        socket.close();
    }
    
    // LOS SIGUIENTES METODOS SON PARA LAS ACCIONES DE LOS BOTONES, RECORDEMOS QUE AL PRINCIPIO DE LA CLASE
    // SE IMPLEMENTARON (ActionListener, KeyListener) PARA RECIBIR LAS ACCIONES QUE EL USUARIO HAGA
    // PRESIONAR UN BOTON O PRESIONAR LA TECLA DE ENTER
    
    // CUANDO SE PRESIONE EL BOTON DE ENVIAR
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals(btnEnviar.getActionCommand())) {
                enviarMensaje(txtMensaje.getText());
            } else if (e.getActionCommand().equals(btnSalir.getActionCommand())) {
                sair();
            }
        } catch (IOException e1) {
        } 
    }
    
    // CUANDO SE PRESIONE LA TECLA ENTER
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                enviarMensaje(txtMensaje.getText());
            } catch (IOException e1) {
            } 
        } 
    }     

    // LA IMPLEMENTACION ME DICE QUE DEBO SOBREESCRIBIR TODOS LOS METODOS DE LA CLASE
    @Override
    public void keyReleased(KeyEvent arg0) { 
    // TODO Auto-generated method stub 
    } 
    
    @Override public void keyTyped(KeyEvent arg0) { 
    // TODO Auto-generated method stub 
    }
    
    // METODO PRINCIPAL, CUANDO SE EJECUTE LLAMARA A LOS METODOS DE CONECTARSE Y LEER LOS MENSAJES QUE SE ESCRIBAN
    public static void main(String[] args) throws IOException {
        Cliente app = new Cliente();
        app.conectarse();
        app.leer();
    }
}
