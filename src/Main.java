import com.google.gson.annotations.SerializedName;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class ApiResponse {
    @SerializedName("conversion_rates")
    private Map<String, Double> conversionRates;

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }
}

public class Main {
    public static void main() {
        int opcion = 0;
        ApiResponse response = fetchExchangeRates();

        // Prepar la interacción con el usuario
        Scanner teclado = new Scanner(System.in);
        double cantidad = 0;
        String menu = """
                Bienvenido al Conversor de Monedas\s
                1 - Dolar -> Peso Mexicano
                2 - Peso Mexicano -> Dolar
                3 - Dolar -> Peso Argentino
                4 - Peso Argentino -> Dolar
                5 - Dolar -> Peso Colombiano
                6 - Peso Colombiano -> Dolar\s
                7 - Salir
               \s""";

        while (opcion != 7) {
            System.out.println(menu);
            while (!teclado.hasNextInt()) {
                System.out.println("Por favor, ingrese el número correspondiente a la conversión.");
                teclado.next(); // Limpiamos el buffer de entrada
            }
            opcion = teclado.nextInt();

            if (opcion >= 1 && opcion <= 6) {
                System.out.println("Ingrese el monto de la divisa que desea convertir:");
                while (!teclado.hasNextDouble()) {
                    System.out.println("Por favor, ingrese un número válido para la cantidad.");
                    teclado.next(); // Limpiamos el buffer de entrada
                }
                cantidad = teclado.nextDouble();
            }

            // Nos aseguramos de que 'response' y 'conversionRates' no son null
            if (response != null && response.getConversionRates() != null) {
                String currency = "";
                boolean invert = false;
                switch (opcion) {
                    case 1 -> currency = "MXN";
                    case 2 -> {
                        currency = "MXN";
                        invert = true;
                    }
                    case 3 -> currency = "ARS";
                    case 4 -> {
                        currency = "ARS";
                        invert = true;
                    }
                    case 5 -> currency = "COP";
                    case 6 -> {
                        currency = "COP";
                        invert = true;
                    }
                    case 7 -> System.out.println("Saliendo del programa, gracias por utilizar nuestros servicios.");
                    default -> System.out.println("Opción no válida. Por favor, intente de nuevo.");
                }
                if (opcion != 7 && !currency.isEmpty()) {
                    Double rate = response.getConversionRates().get(currency);
                    if (rate != null) {
                        double cambio = invert ? cantidad / rate : cantidad * rate;
                        System.out.printf("La cantidad es: $%.2f%n", cambio);
                    } else {
                        System.out.printf("No se encontró la tasa de cambio para %s.%n", currency);
                    }
                }
            } else {
                System.out.println("No se pudo obtener las tasas de cambio de la API.");
            }
        }
        teclado.close();
    }

    private static ApiResponse fetchExchangeRates() {
        String apiUrl = "https://v6.exchangerate-api.com/v6/8f6387c9c44a882effb51a02/latest/USD";
        try {
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Ha ocurrido un error: " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder informationString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    informationString.append(line);
                }

                System.out.println("Respuesta cruda de la API: " + informationString);

                Gson gson = new Gson();
                ApiResponse response = gson.fromJson(informationString.toString(), ApiResponse.class);
                if (response.getConversionRates() == null) {
                    System.out.println("Conversion rates are null.");
                    return null;
                }
                return response;
            } catch (JsonSyntaxException e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error fetching exchange rates: " + e.getMessage());
            return null;
        }
    }
}
