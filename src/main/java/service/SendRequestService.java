package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.ResultStatus;
import model.Ticket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import util.Utils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:request.properties")
public class SendRequestService {

    private String centralServerUrl = "http://entegrationsais.csb.gov.tr/SAIS";
    public static Ticket ticket;

    @Value( "${simUserName}" )
    private String simUserName;

    @Value( "${simPassword}" )
    private String simPassword;


    public void login() throws URISyntaxException, JsonProcessingException, NoSuchAlgorithmException {
        HashMap<String,String> body = new HashMap<>();
        body.put("username", simUserName);
        body.put("password", Utils.encriyptMD5(Utils.encriyptMD5(simPassword)));
        ResultStatus resultStatus = sendRequest("/Security/login",body);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ticket = mapper.convertValue(resultStatus.getObjects(), Ticket.class);
    }

    public Ticket getTicket() throws URISyntaxException, JsonProcessingException, NoSuchAlgorithmException {
        if(ticket == null)
            login();
        return ticket;
    }

    public ResultStatus sendRequest(String path) throws URISyntaxException, JsonProcessingException, NoSuchAlgorithmException {
        return sendRequest(path,null,null);
    }

    public ResultStatus sendRequest(String path, Map body) throws URISyntaxException, JsonProcessingException, NoSuchAlgorithmException {
        return sendRequest(path,body,null);
    }

    public ResultStatus sendRequest(String path, Map body, MediaType mediaType) throws URISyntaxException, JsonProcessingException, NoSuchAlgorithmException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        if(mediaType == null)
            headers.setContentType(MediaType.APPLICATION_JSON);
        else
            headers.setContentType(mediaType);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // Her defasında "Atoken" setliyoruz, login hariç. "
        String url = null;
        if(!path.endsWith("login"))
        {
            HashMap<String,String> ticketBody = new HashMap<>();
            ticketBody.put("TicketId",getTicket().getTicketId());
            headers.add("AToken", new ObjectMapper().writeValueAsString(ticketBody));
            url = centralServerUrl + path;
        }else{ // login ise
            url= centralServerUrl.substring(0,centralServerUrl.lastIndexOf('/')) + path;
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<ResultStatus> result = null;
        // Eğer ticket id 30 dk geçtikten sonra expire olmuşsa 401 hatası dönüyor. bu udurmda yeniden ticket id alması için
        // loginService.login() metodunu çağırıyoruz
        try {
            result = restTemplate.postForEntity(url, entity, ResultStatus.class);
        }catch (HttpClientErrorException e){
            if(e.getRawStatusCode() == 401)
            {
                login();
                sendRequest(path,body,mediaType);
            }
        }catch(Exception e1){
            System.out.println(url + " --- " + e1.getStackTrace());
        }

        return result == null ? null : result.getBody();
    }
}
