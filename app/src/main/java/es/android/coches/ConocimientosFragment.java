package es.android.coches;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import es.android.coches.databinding.FragmentConocimientosBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ConocimientosFragment extends Fragment {

    private FragmentConocimientosBinding binding;

    List<Pregunta> todasLasPreguntas;
    List<String> todasLasRespuestas;

    List<Pregunta> preguntas;
    int respuestaCorrecta;

    int respuestaAcertada;
    String puntuacionSuperada;
    String jsonPrueba;
    Puntuacion puntuacion = new Puntuacion();
    Gson g = new Gson();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(todasLasPreguntas == null) {
            try {
                generarPreguntas("coches.xml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.shuffle(todasLasPreguntas);
        preguntas = new ArrayList<>(todasLasPreguntas);

        if(!fileExists(getContext(), "Puntuacion.json")){


             g.toJson(puntuacion.getPuntuacion_maxima());
             g.toJson(puntuacion.getUltima_puntuacion());
             jsonPrueba = g.toString();
             salvarFichero("Puntacion.json", jsonPrueba);
        }else{
            String fichero = "";
            try {
                InputStream is  = getContext().openFileInput("Puntuacion.json");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String Linea;
                while ((Linea = br.readLine()) != null) {
                    fichero += Linea;
                }
                Properties properties = g.fromJson(fichero, Properties.class);
                puntuacion.setUltima_puntuacion((Integer) properties.get("Ultima_puntuacion"));
                puntuacion.setPuntuacion_maxima((Integer) properties.get("Puntuacion_maxima"));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    private void salvarFichero(String fichero, String texto) {
        FileOutputStream fos;
        try {
            fos = getContext().openFileOutput(fichero, Context.MODE_PRIVATE);
            fos.write(texto.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConocimientosBinding.inflate(inflater,container,false);

        presentarPregunta();

        binding.botonRespuesta.setOnClickListener(v -> {
            int seleccionado = binding.radioGroup.getCheckedRadioButtonId();
            CharSequence mensaje;
            if(seleccionado == respuestaCorrecta){
             mensaje = "¡Acertaste!";
             respuestaAcertada++;
             puntuacion.setPuntuacion_maxima(respuestaAcertada);

                if(puntuacion.getPuntuacion_maxima() > puntuacion.getUltima_puntuacion()){
                    puntuacion.setUltima_puntuacion(respuestaAcertada);
                    puntuacionSuperada = "¡Has batido tu récord de aciertos! Has alcanzado: " + puntuacion.getUltima_puntuacion() + " puntos";
                }
            }else{
             mensaje = "¡Fallaste!";
            }
            Snackbar.make(v, mensaje, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Siguiente", v1 -> presentarPregunta())
                    .show();
            v.setEnabled(false);
        });

        return binding.getRoot();
    }

    private List<String> generarRespuestasPosibles(String respuestaCorrecta) {
        List<String> respuestasPosibles = new ArrayList<>();
        respuestasPosibles.add(respuestaCorrecta);

        List<String> respuestasIncorrectas = new ArrayList<>(todasLasRespuestas);
        respuestasIncorrectas.remove(respuestaCorrecta);

        for(int i=0; i<binding.radioGroup.getChildCount()-1; i++) {
            int indiceRespuesta = new Random().nextInt(respuestasIncorrectas.size());
            respuestasPosibles.add(respuestasIncorrectas.remove(indiceRespuesta));

        }
        Collections.shuffle(respuestasPosibles);
        return respuestasPosibles;
    }

    private void presentarPregunta() {
        if(preguntas.size() > 0) {
            binding.botonRespuesta.setEnabled(true);

            int pregunta = new Random().nextInt(preguntas.size());

            Pregunta preguntaActual = preguntas.remove(pregunta);
            preguntaActual.setRespuetas(generarRespuestasPosibles(preguntaActual.respuestaCorrecta));

            InputStream bandera = null;
            try {
                int idLogo = getResources().getIdentifier(preguntaActual.foto,"raw",getContext().getPackageName());
                bandera = getContext().getResources().openRawResource(idLogo);
                binding.bandera.setImageBitmap(BitmapFactory.decodeStream(bandera));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // anadir
            binding.radioGroup.clearCheck();
            for (int i = 0; i < binding.radioGroup.getChildCount(); i++) {
                RadioButton radio = (RadioButton) binding.radioGroup.getChildAt(i);
                // comentar
                // radio.setChecked(false);
                CharSequence respuesta = preguntaActual.getRespuetas().get(i);
                if (respuesta.equals(preguntaActual.respuestaCorrecta))
                    respuestaCorrecta = radio.getId();

                radio.setText(respuesta);
            }
        } else {

            g.toJson(puntuacion.getPuntuacion_maxima());
            g.toJson(puntuacion.getUltima_puntuacion());
            jsonPrueba = g.toString();
            salvarFichero("Puntacion.json", jsonPrueba);

            String finalPuntos = "Has conseguido: " + puntuacion.getPuntuacion_maxima() + " puntos";

            binding.bandera.setVisibility(View.GONE);
            binding.radioGroup.setVisibility(View.GONE);
            binding.botonRespuesta.setVisibility(View.GONE);

            if(puntuacionSuperada == null) binding.textView.setText("¡Fin!" + finalPuntos);
            else binding.textView.setText("¡Fin!" + puntuacionSuperada);
        }
    }


    class Pregunta {
        private String nombre;
        private String foto;
        private String respuestaCorrecta;
        private List<String> respuetas;

        public Pregunta(String nombre, String foto) {
            this.nombre = nombre;
            this.foto = foto;
            this.respuestaCorrecta = nombre;
        }

        public List<String> getRespuetas() {
            return respuetas;
        }

        public void setRespuetas(List<String> respuetas) {
            this.respuetas = respuetas;
        }
    }

    private Document leerXML(String fichero) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder constructor = factory.newDocumentBuilder();
        int idRecurso = getResources().getIdentifier("coches", "raw", getContext().getPackageName());
        Document doc = constructor.parse(getContext().getResources().openRawResource(idRecurso));
        doc.getDocumentElement().normalize();
        return doc;
    }

    private void generarPreguntas(String fichero) throws Exception {
        todasLasPreguntas = new LinkedList<>();
        todasLasRespuestas = new LinkedList<>();
        Document doc = leerXML(fichero);
        Element documentElement = doc.getDocumentElement();
        NodeList paises = documentElement.getChildNodes();
        for(int i=0; i<paises.getLength(); i++) {
            if(paises.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element pais = (Element) paises.item(i);
                //String nombre = pais.getAttribute("nombre");
                String nombre = pais.getElementsByTagName("nombre").item(0).getTextContent();
                String foto = pais.getElementsByTagName("foto").item(0).getTextContent();
                todasLasPreguntas.add(new Pregunta(nombre, foto));
                todasLasRespuestas.add(nombre);
            }
        }
    }
}