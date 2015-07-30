package tts.br.pronouncelearner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *    Este codigo trata-se de um tutorial para o uso da ferramenta
 * de sintese de audio a partir de texto do Android: o TextToSpeech.
 *
 *    A classe MainActivity e os xmls activity_main e menu_main
 * utilizadas foram criadas automaticamente pelo Android Studio ao se iniciar
 * um novo projeto com o padrao Blank Activity with Fragment e nao foram modificadas
 * para esse tutorial.
 *
 *    E importante ressaltar que esse codigo poderia ser implantado na classe
 * MainActivity tambem, sem a necessidade do fragment, mas fazendo as devidas alteracoes
 *
 *    Nao e necessario instalar nenhum pacote ou biblioteca especial para usar o basico
 * do TextToSpeech, apenas importar a classe android.speech.tts.TextToSpeech;
 *
 *    Idiomas disponiveis pela Engine usada, nao necessariamente estarao instalados no
 * device. Logo, eh importante saber que os dados para instalacao de um idioma disponivel
 * no caso da engine do Android sao baixados e instalados automaticamente quando ha internet
 *
 *    Funcoes atualmente deprecated nao foram trabalhadas, a excecao
 * da funcao speak(...).
 *
 * @author : Joao Pedro de C. Magalhaes
 * github  : jpedrocm
 * */

public class MainActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener,
            View.OnClickListener, TextToSpeech.OnInitListener{

    /**
     *  Essa classe implementa:
     *    - AdapterView.OnItemSelectedListener para a selecao de um item
     *      do spinner;
     *    - View.OnClickListener para o clicar do botao, onde sera chamada
     *      a funcao de leitura do texto;
     *    - TextToSpeech.OnInitListener para a inicializacao do TextToSpeech.
     *  */

    Spinner sp_idioma; // equivalente a uma combobox, vai conter os idiomas

    EditText et_texto; // onde o texto sera digitado pelo usuario

    Button btn_ler; // botao usado para chamar a leitura do texto

    ArrayAdapter<CharSequence> adapter; // array adapter simples para preencher o spinner

    TextToSpeech tts; /* a ferramenta propriamente dita, chamarei de TTS,
                         suas funcoes so devem ser chamadas apos o sucesso
                         da inicializacao */

    String idiomaAtual; // representara o idioma atual

    Locale[] locais; // representara os locais disponiveis

    ArrayList<Locale> idiomas; //representara a lista de idiomas do tts

    static int flushDaFila = TextToSpeech.QUEUE_FLUSH; /* inteiro que representa um dos modos
                                                  de se preencher o buffer de leitura do tts. Com
                                                  esse modo, o buffer eh esvaziado primeiro e depois
                                                  adiciona-se o novo texto. Nesse modo, se houvesse algum
                                                  texto ainda nao lido, mas no buffer, ele seria ignorado */

    static int semFlushDaFila = TextToSpeech.QUEUE_ADD; /* inteiro que representa a alternativa ao anterior.
                                                   Ele apenas adiciona ao fim do buffer o novo texto. */

    static int idiomaDisponivel = TextToSpeech.LANG_AVAILABLE; // inteiro que representa um idioma disponivel

    static int sucesso = TextToSpeech.SUCCESS; // inteiro que representa o sucesso de uma acao no uso do TTS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        sp_idioma = (Spinner) rootView.findViewById(R.id.sp_idioma);

        et_texto = (EditText) rootView.findViewById(R.id.et_texto);

        btn_ler = (Button) rootView.findViewById(R.id.btn_ler);

        tts = new TextToSpeech(getActivity(), this); /* inicializacao do objeto TTS, que
          recebe como parametros o contexto e o OnInitListener. O contexto nesse caso
          eh a activity e o this chamara automaticamente o listener pois a classe o implementa.
           Essa chamada usara a engine padrao do device, para usar outra engine,
           deve-se passar o terceiro argumento (o nome do pacote da engine, uma string) */

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.idiomas_array, android.R.layout.simple_spinner_item);
        /* alguns dos idiomas disponiveis se encontram no string-array dentro do xml arrays, mas tambem podem ser achados
           como mostrado na funcao locaisComIdioma() */

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_idioma.setAdapter(adapter);
        sp_idioma.setOnItemSelectedListener(this);

        idiomaAtual = "ingles(US)";

        locais = Locale.getAvailableLocales(); /* pega todos os locais disponiveis, e
                                      serao utilizados para achar os idiomas disponiveis*/

        btn_ler.setOnClickListener(this);

        return rootView;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        /* aqui esta detalhado o que acontece ao selecionar um item do spinner.
        *  Apos a selecao de um item do spinner, setamos o idioma da engine. */

        idiomaAtual = (String) parent.getItemAtPosition(position);

         /* abaixo se encontra uma lista basica com alguns dos idiomas e variantes disponiveis
           na engine padrao do Android. Para saber quais estao disponiveis na engine que vc usa,
            basta procurar por "idioma" no logcat */

        if(idiomaAtual.equals("Ingles(US)")){
            this.defineIdioma(Locale.US);
        } else if(idiomaAtual.equals("Ingles(UK)")){
            this.defineIdioma(Locale.UK);
        } else if(idiomaAtual.equals("Italiano")){
            this.defineIdioma(Locale.ITALY);
        } else if(idiomaAtual.equals("Frances(FR)")){
            this.defineIdioma(Locale.FRANCE);
        } else if(idiomaAtual.equals("Coreano")){
            this.defineIdioma(Locale.KOREA);
        } else if(idiomaAtual.equals("Japones")) {
            this.defineIdioma(Locale.JAPAN);
        } else if(idiomaAtual.equals("Chines")){
            this.defineIdioma(Locale.CHINA);
        } else if(idiomaAtual.equals("Frances(CA)")) {
            this.defineIdioma(Locale.CANADA_FRENCH);
        } else if(idiomaAtual.equals("Alemao")) {
            /* para os idiomas mais usados (esse e os acima), existem Locales faceis, estaticos
             que podem ser usados como parametro. */
            this.defineIdioma(Locale.GERMANY);
        } else if(idiomaAtual.equals("Espanhol")){
            /* a partir daqui (esse e abaixo), eh necessario criar o Locale do zero ou buscar
            no ArrayList que criamos nessa classe */

            this.defineIdioma(new Locale("es"));
        } else if(idiomaAtual.equals("Portugues(PT)")) {
            this.defineIdioma(new Locale("pt", "PT"));
        } else if(idiomaAtual.equals("Portugues(BR)")) {
            this.defineIdioma(new Locale("pt", "BR"));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onClick(View v) {

        // aqui esta detalhado o que acontece apos clicar no botao de leitura do texto

        String texto = et_texto.getText().toString().trim();

        if(texto.equals("")){

            // Apenas cria um alerta para caso nao haja texto a ser lido

            new AlertDialog.Builder(getActivity()).setTitle("Falha!")
                    .setMessage("Digite algum texto.")
                    .setIcon(R.id.alertTitle)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which){}}).show();
            return;
        }

        this.ler(texto); // para ler o texto
    }

    @Override
    public void onInit(int status) {

        // aqui saberemos se o tts foi inicializado ou nao

        if(status==sucesso){
            //sucesso de inicializacao

            btn_ler.setEnabled(true); /* torna o botao disponivel
            para clicar pois houve sucesso na inicializacao */

            idiomas = this.locaisComIdioma();

        } else {
            // erro de inicializacao
        }
    }


    /*    A partir daqui temos as funcoes que podem ser utilizadas com o tts.
     * Elas foram encapsuladas dentro de metodos da classe Fragment apenas com proposito
     * de organizar a explicacao */

    public String ttsEnginePadrao(){
        // retorna o nome do pacote da Engine padrao utilizado pelo device
        return tts.getDefaultEngine();
    }

    public List<TextToSpeech.EngineInfo> ttsEngines(){
        /*    retorna uma lista com informacoes todas as engines tts instaladas. Cada
          elemento da lista e um TextToSpeech.EngineInfo, e as informacoes disponiveis sao:
             - icon: icone da engine
             - label: o nome da engine
             - name: pacote onde esta a engine
         */
        return tts.getEngines();
    }

    public int idiomaDisponivel(Locale locale){

        /*    checa se determinado idioma esta disponivel no tts,
        *  usando um objeto Locale como parametro. E interessante
        *  lembrar que os idiomas disponiveis dependem diretamente
        *  da engine que esta sendo utilizada.
         *     Essa funcao pode retornar (entre parenteses sao constantes da classe TTS):
         *     - 0 (LANG_AVAILABLE), idioma disponivel, mas nao do pais e variante
          *    - 1 (LANG_COUNTRY_AVAILABLE), idioma e pais disponiveis, variante nao
           *   - 2 (LANG_COUNTRY_VAR_AVAILABLE), idioma disponivel como especificado pelo locale
           *   - (-1) (LANG_MISSING_DATA), idioma nao pode ser utilizado por falta de dados
           *   - (-2) (LANG_NOT_SUPPORTED), idioma nao pode ser utilizado por nao ser suportado */

        return tts.isLanguageAvailable(locale);
    }

    public int defineIdioma(Locale locale){
        /*    Metodo que define o idioma a ser usado pela engine utilizada.
        *  Retorna um inteiro assim como isLanguageAvailable(...) */
        return tts.setLanguage(locale);
    }

    public boolean estaLendo(){
        // checa se o tts esta ocupado lendo algum texto
        return tts.isSpeaking();
    }

    public void defineVelocidadeDeLeitura(float velocidade){
        // define a velocidade de leitura
        tts.setSpeechRate(velocidade);
    }

    public void defineAlturaDeLeitura(float altura){
        // define a altura (pitch) da leitura
        tts.setPitch(altura);
    }

    public int pararLeitura(){
        /* metodo para parar a leitura de texto, retorna um inteiro
         indicando sucesso ou erro */
        return tts.stop();
    }

    public int ler(String texto){
        /* metodo para ler o texto por versao do OS.
           Retorna um inteiro para dizer se houve erro na adicao ao buffer ou sucesso.
           Sucesso eh a variavel estatica sucesso desta classe. */

        String versao = Build.VERSION.RELEASE; // descobre a versao do OS

        if(versao.startsWith("5")){
            // em caso de versao 5.0 ou 5.1 (ou seja, API 21 e 22)
            return tts.speak(texto, flushDaFila, null, null);
            /* nesse caso, os parametros sao:
            *  - CharSequence: texto a ser lido
            *  - int: modo de adicao ao buffer
            *  - Bundle: pode ser null ou podem ser argumentos relacionados ao som, como volume
            *  - String: para identificar, a seu criterio e caso necessario, a request de leitura */
        } else {
            // para as outras APIs
            return tts.speak(texto, flushDaFila, null);
            /* nesse caso, os parametros sao:
            *  - String: texto a ser lido
            *  - int: modo de adicao ao buffer
            *  - HashMap<String,String>: funciona como o Bundle da API 21 */
        }

    }

    public int quantidadeMaximaDeCaracteres(){
        /* essa funcao so pode ser usada a partir da API 18, e permite
         saber o limite maximo de caracteres que o tts consegue ler */
       return tts.getMaxSpeechInputLength();
    }

    public void desligar(){
        /* metodo para deixar de usar de vez o TTS, tornando disponiveis os recursos
            que ele utiliza. Interessante chama-lo no metodo OnDestroy
             do seu fragment ou activity */
        tts.shutdown();
    }

    public ArrayList<Locale> locaisComIdioma(){

        /*    Essa funcao foi criada por mim para saber todos os idiomas disponiveis
           na engine tts. Trata-se de uma boa alternativa para quem ainda nao usa a API 21.
           Na API 21 ja existe uma funcao chamada getAvailableLanguages */

        ArrayList<Locale> listaDeIdiomas = new ArrayList<>();

        for(int i = 0; i < locais.length; i++){
            /* para cada local achado por getAvailableLocales, testamos se o idioma esta disponivel.
               Resultados positivos indicam presenca do idioma */
            if(this.idiomaDisponivel(locais[i]) >= idiomaDisponivel){
                listaDeIdiomas.add(locais[i]);
                Log.d("idioma",locais[i].getLanguage() + " " + locais[i].getCountry());
            }
        }

        return listaDeIdiomas;
    }
}