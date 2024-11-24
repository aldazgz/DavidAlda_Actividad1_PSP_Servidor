import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorBiblioteca {
    // Puerto para la conexión del servidor
    private static final int PUERTO = 8080;
    private static final List<Libro> libros = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        inicializarLibros(); // Inicializa la lista de libros

        // Servidor que usa el puerto definido
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            // Acepta la conexión con el cliente y crea un hilo para la comunicación
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());
                new Thread(new ManejadorCliente(clienteSocket)).start();
            }
            // Excepciones
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   //Carga de libros
    private static void inicializarLibros() {
        libros.add(new Libro("11111", "El Quijote", "Miguel de Cervantes", 10.99));
        libros.add(new Libro("22222", "Cien Años de Soledad", "Gabriel García Márquez", 12.99));
        libros.add(new Libro("33333", "1984", "George Orwell", 8.99));
        libros.add(new Libro("44444", "Juego de Tronos", "GRR Martin", 5.99));
        libros.add(new Libro("55555", "El Retorno del Rey", "JRR Tolkien", 7.99));
        libros.add(new Libro("66666", "Las Dos Torres", "JRR Tolkien", 7.99));
        libros.add(new Libro("77777", "La Comunidad del Anillo", "JRR Tolkien", 7.99));

    }
    // Clase para manejar la conexión y la lógica del cliente
    private static class ManejadorCliente implements Runnable {
        private final Socket clienteSocket;

        public ManejadorCliente(Socket clienteSocket) {
            this.clienteSocket = clienteSocket;
        }

        @Override
        public void run() {
            try (ObjectInputStream entrada = new ObjectInputStream(clienteSocket.getInputStream());
                 ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream())) {

                String opcion;
                while ((opcion = (String) entrada.readObject()) != null) {
                    switch (opcion) {
                        case "1":
                            String isbn = (String) entrada.readObject();
                            salida.writeObject(buscarPorISBN(isbn));
                            break;
                        case "2":
                            String titulo = (String) entrada.readObject();
                            salida.writeObject(buscarPorTitulo(titulo));
                            break;
                        case "3":
                            String autor = (String) entrada.readObject();
                            salida.writeObject(buscarPorAutor(autor));
                            break;
                        case "4":
                            String libroData = (String) entrada.readObject();
                            String mensaje = agregarLibro(libroData);
                            salida.writeObject(mensaje);
                            break;
                        case "salir":
                            salida.writeObject("Desconexión exitosa.");
                            clienteSocket.close();
                            return;
                        default:
                            salida.writeObject("Opción inválida.");
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        // Métodos
        private String buscarPorISBN(String isbn) {
            synchronized (libros) {
                return libros.stream()
                        .filter(libro -> libro.getIsbn().equalsIgnoreCase(isbn))
                        .findFirst()
                        .map(Libro::toString)
                        .orElse("No se encontró un libro con ese ISBN.");
            }
        }

        private String buscarPorTitulo(String titulo) {
            synchronized (libros) {
                return libros.stream()
                        .filter(libro -> libro.getTitulo().equalsIgnoreCase(titulo))
                        .findFirst()
                        .map(Libro::toString)
                        .orElse("No se encontró un libro con ese título.");
            }
        }

        public static synchronized List<String> buscarPorAutor(String autor) {
            List<String> resultados = new ArrayList<>();
            for (Libro libro : libros) {
                if (libro.getAutor().equalsIgnoreCase(autor)) {
                    resultados.add(libro.toString());
                }
            }
            return resultados.isEmpty() ? null : resultados;
        }

        private synchronized String agregarLibro(String libroData) {
            String[] datos = libroData.split(",");

            if (datos.length != 4) {
                return "Datos del libro incorrectos. Formato esperado: ISBN,Título,Autor,Precio.";
            }
            String isbn = datos[0];
            String titulo = datos[1];
            String autor = datos[2];
            double precio;

            try {
                precio = Double.parseDouble(datos[3]);
            } catch (NumberFormatException e) {
                return "El precio debe ser un número válido.";
            }
            Libro nuevoLibro = new Libro(isbn, titulo, autor, precio);
            libros.add(nuevoLibro);
            return "Libro añadido correctamente.";
        }

    }
}