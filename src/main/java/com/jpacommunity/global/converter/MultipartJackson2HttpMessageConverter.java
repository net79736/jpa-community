package com.jpacommunity.global.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * https://chatgpt.com/c/676fda6d-2b38-8004-82da-755b2d23e6b5
 *
 * MultipartJackson2HttpMessageConverter는 멀티파트 요청을 처리하는 역할을 합니다.
 * 이 요청에는 일반적으로 다음과 같은 데이터가 포함됩니다:
 *
 * 1. JSON 데이터
 * 2. 파일(바이너리 데이터)
 * 멀티파트 요청에서 파일은 바이너리 데이터로 처리되므로, 이를 적절히 처리하기 위해
 * **MediaType.APPLICATION_OCTET_STREAM**이 기본값으로 설정된 것입니다.
 *
 * MultipartJackson2HttpMessageConverter 는 멀티파트 요청에서 JSON 데이터를 처리하기 위해 Jackson의 ObjectMapper를 활용합니다.
 * JSON 데이터는 일반적으로 application/json 타입으로 처리되지만,
 * 멀티파트 요청에서는 각 파트가 고유의 타입(예: application/octet-stream, application/json, text/plain)을 가질 수 있습니다.
 * 따라서 이 컨버터는 멀티파트 요청 내의 JSON 데이터와 파일 데이터 모두를 처리할 수 있도록 설계되었습니다.
 *
 *
 * AbstractJackson2HttpMessageConverter는 Spring에서 Jackson 라이브러리를 사용해 JSON 데이터를 변환하는 기본 클래스입니다.
 * 이 클래스를 상속하면 JSON을 처리하는 Jackson의 동작을 커스터마이징할 수 있습니다.
 * 이 커스터마이징된 메시지 컨버터는 주로 멀티파트 요청에서 JSON 데이터를 파싱할 때 사용됩니다.
 * 예를 들어, 멀티파트 요청의 일부가 JSON 데이터인 경우 이를 처리하기 위해 필요한 역할을 수행합니다.
 * MediaType.APPLICATION_OCTET_STREAM
 * 이 컨버터는 기본적으로 application/octet-stream 미디어 타입(바이너리 데이터 형식)을 처리하도록 설정되어 있습니다.
 * application/octet-stream은 멀티파트 데이터를 처리할 때 자주 사용됩니다.
 * canWrite 메서드의 비활성화
 *
 * canWrite 메서드가 모두 false를 반환하도록 설정했습니다. 이는 이 컨버터가 요청 본문을 JSON으로 변환하는 "쓰기" 작업을 하지 않도록 제한한 것입니다.
 * 즉, 이 컨버터는 요청 데이터를 읽기만 하고 응답 데이터는 변환하지 않습니다.
 *
 * 메시지 컨버터 자동 선택
 *      Spring은 요청의 Content-Type 에 따라 적절한 메시지 컨버터를 자동으로 선택합니다:
 *
 *      application/json → MappingJackson2HttpMessageConverter
 *      text/plain → StringHttpMessageConverter
 *      멀티파트 요청 → MultipartJackson2HttpMessageConverter
 *
 * 이 빈이 필요한 이유
 * Spring MVC의 메시지 컨버터
 *
 * Spring은 요청 본문을 처리할 때 등록된 HttpMessageConverter 목록을 순회하며 적합한 컨버터를 찾습니다.
 * 기본적으로 Spring은 멀티파트 요청에서 JSON 데이터를 처리할 때 Jackson 기반 컨버터를 자동으로 선택하지 않습니다.
 * 따라서, 멀티파트 요청과 JSON 처리를 동시에 하기 위해 이 커스터마이징된 컨버터를 추가한 것입니다.
 * 멀티파트 요청에서 JSON 처리
 *
 * 일반적으로 멀티파트 요청에서 JSON 데이터를 처리하려면, 멀티파트 요청의 일부 데이터를 JSON으로 변환하는 커스터마이징이 필요합니다.
 * 이 클래스는 멀티파트 요청 본문에 포함된 JSON 데이터를 처리하도록 설정되었습니다.
 * Jackson과 멀티파트의 호환성 문제 해결
 *
 * 멀티파트 요청에서 JSON 데이터를 처리하려면 Spring이 Jackson을 통해 JSON 데이터를 제대로 파싱하도록 설정해야 합니다.
 * 이 클래스는 ObjectMapper와 연결하여 JSON 데이터를 읽는 역할을 합니다.
 *
 * 이 빈의 동작 요약
 * 1. 멀티파트 요청이 들어오면 Spring 이 적절한 HttpMessageConverter 를 찾습니다.
 * 2. 이 빈은 멀티파트 요청의 application/octet-stream 또는 JSON 부분을 처리할 때 사용됩니다.
 * 3. 요청 데이터 중 JSON 데이터를 Jackson 을 사용해 자동으로 객체로 변환합니다.
 * 4. "쓰기" 작업은 비활성화되므로 응답 데이터에는 영향을 미치지 않습니다. 즉, 요청 데이터를 읽기만 하고, 응답 처리에는 관여하지 않는다는 의미입니다.
 *
 * 예시 상황: 멀티파트 요청 처리
 * 1. 요청(Request):
 *
 *   클라이언트가 멀티파트 요청으로 JSON 데이터와 파일을 서버로 전송합니다.
 *   요청 데이터:
 *     POST /api/upload
 *     Content-Type: multipart/form-data; boundary=abc123
 *
 *     --abc123
 *     Content-Disposition: form-data; name="jsonData"
 *     Content-Type: application/json
 *
 *     {"title": "Test Title", "description": "Test Description"}
 *     --abc123
 *     Content-Disposition: form-data; name="file"; filename="test.txt"
 *     Content-Type: text/plain
 *
 *     Hello, World!
 *     --abc123--
 *
 * 2. 서버에서 처리:
 *
 *   서버에서는 MultipartJackson2HttpMessageConverter 를 통해:
 *   jsonData를 읽어 {"title": "Test Title", "description": "Test Description"}을 Java 객체로 변환.
 *   파일 데이터는 그대로 처리.
 * 3. 응답(Response):
 *
 * 이 컨버터는 "쓰기 작업을 하지 않으므로" 응답 데이터를 변환하지 않습니다.
 * 예를 들어, 컨트롤러가 아래와 같은 응답을 반환한다고 가정하면:
 *
 * @PostMapping("/upload")
 * public ResponseEntity<String> upload(@RequestPart("jsonData") JsonData jsonData, @RequestPart("file") MultipartFile file) {
 *     return ResponseEntity.ok("Upload successful");
 * }
 *
 * 이 컨버터는 응답 데이터 "Upload successful"을 JSON으로 변환하거나 다른 작업을 하지 않습니다. 단순히 컨트롤러가 반환하는 데이터가 클라이언트로 그대로 전달됩니다.
 *
 * AbstractJackson2HttpMessageConverter는 Jackson을 기반으로 HTTP 요청/응답의 JSON 처리를 담당합니다. 이 클래스는 구체적으로 사용되지 않으며, 이를 확장한 서브 클래스들이 실제로 동작합니다.
 *
 * 주요 서브 클래스:
 *      1. MappingJackson2HttpMessageConverter
 *          * JSON 데이터를 Java 객체로 변환하거나 그 반대로 변환.
 *          * 주로 application/json Content-Type을 처리.
 *      2. MappingJackson2XmlHttpMessageConverter
 *          * XML 데이터를 Jackson으로 처리.
 *      3. MultipartJackson2HttpMessageConverter
 *          * 멀티파트 요청에서 JSON 데이터를 처리.
 *
 *  Spring의 HTTP 요청/응답에서 JSON을 처리할 때 HttpMessageConverter가 작동하며, 그 중심에 Jackson 기반의 컨버터가 있습니다.
 *
 *  요청 처리 (Deserialization):
 *      1. 클라이언트가 application/json 요청 본문을 서버로 보냅니다.
 *      2. AbstractJackson2HttpMessageConverter 의 서브 클래스가 이를 처리하고, Jackson의 ObjectMapper를 사용해 요청 본문을 Java 객체로 변환합니다.
 *  응답 처리 (Serialization):
 *      1. 서버는 Java 객체를 응답 본문으로 변환합니다.
 *      2. AbstractJackson2HttpMessageConverter의 서브 클래스가 Jackson의 ObjectMapper를 사용해 Java 객체를 JSON으로 직렬화합니다.
 *      3. JSON 응답은 클라이언트로 전송됩니다.
 *
 *  AbstractJackson2HttpMessageConverter는 추상 클래스로, 하위 클래스에서 오버라이드하거나 활용할 수 있는 여러 메서드를 제공합니다.
 *      canRead(Class<?> clazz, MediaType mediaType):
 *          특정 클래스와 MIME 타입의 데이터를 읽을 수 있는지 확인.
 *      canWrite(Class<?> clazz, MediaType mediaType):
 *          특정 클래스와 MIME 타입의 데이터를 쓸 수 있는지 확인.
 *      read(Class<? extends T> clazz, HttpInputMessage inputMessage):
 *          HTTP 요청 본문을 읽고, JSON 데이터를 Java 객체로 변환.
 *      write(T t, MediaType contentType, HttpOutputMessage outputMessage):
 *          Java 객체를 JSON으로 직렬화하고 HTTP 응답 본문에 작성.
 *      getObjectMapper():
 *          Jackson ObjectMapper를 반환하며, 커스터마이징할 수 있음.
 *
 *  Spring은 요청/응답 처리를 위해 **메시지 컨버터 체인(message converter chain)**을 사용합니다.
 *  여러 메시지 컨버터가 등록되어 있을 경우, 각 컨버터는 요청의 Content-Type 과 타겟 클래스를 기준으로 자신이 처리할 수 있는 작업만 수행합니다.
 *
 *  MultipartJackson2HttpMessageConverter(기본 비활성화 상태) 를 추가해도 다음과 같은 원칙에 따라 작동하므로 충돌이 발생하지 않습니다:
 *
 *  1. Content-Type 기반 선택:
 *      * 멀티파트 요청(multipart/form-data)만 처리 대상으로 삼습니다.
 *      * 일반 JSON 요청(application/json)은 여전히 MappingJackson2HttpMessageConverter가 처리합니다.
 *  2. 타겟 미디어 타입 검사:
 *      * MultipartJackson2HttpMessageConverter 는 멀티파트 요청에서 JSON 이나 바이너리 데이터를 처리하기 위한 컨버터입니다.
 *      * 다른 메시지 컨버터(예: StringHttpMessageConverter, MappingJackson2HttpMessageConverter)는 여전히 자신의 대상 타입(text/plain, application/json)만 처리합니다.
 *  3. Spring의 메시지 컨버터 체인:
 *      * Spring 은 등록된 컨버터를 순차적으로 검사하며, 적합한 컨버터를 선택합니다. 따라서 새로운 컨버터를 추가해도 기존 컨버터의 동작에 영향을 미치지 않습니다.
 *
 *
 *
 */
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    /**
     * ObjectMapper 를 받아서 부모 클래스(AbstractJackson2HttpMessageConverter)에 전달합니다.
     * MediaType.APPLICATION_OCTET_STREAM 을 처리할 수 있도록 설정합니다.
     *
     * Converter for support http request with header Content-Type: multipart/form-data
     */
    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        // application/octet-stream 은 MIME 타입으로, 임의의 바이너리 데이터를 나타냅니다.
        // 이 타입은 특정한 데이터 형식이 정해지지 않은 파일(예: 바이너리 파일, 이미지, 압축 파일 등)을 전송할 때 사용됩니다.
        // 이 코드는 멀티파트 요청에서 JSON 데이터를 처리하기 위해 MultipartJackson2HttpMessageConverter 를 초기화하는 생성자입니다.
        // * ObjectMapper: Jackson을 사용해 JSON 데이터를 Java 객체로 변환하거나, 그 반대로 변환하는 도구.
        // * MediaType.APPLICATION_OCTET_STREAM: 멀티파트 요청 중 파일이나 바이너리 데이터를 기본적으로 처리할 타입.
        // 즉, 이 생성자는 멀티파트 요청 내에서 JSON 데이터와 파일을 처리할 수 있도록 설정합니다.
        // 1. 멀티파트 요청에서 JSON 데이터를 명확히 처리하려는 경우
        //  기본적으로 Spring 은 멀티파트 요청에서 JSON 과 파일을 각각 다른 컨버터로 처리합니다:
        //      * JSON 데이터 → MappingJackson2HttpMessageConverter
        //      * 파일 데이터 → StandardServletMultipartResolver
        //  하지만, 멀티파트 요청의 JSON 데이터를 보다 명확하게 처리하거나 커스터마이징하려면 MultipartJackson2HttpMessageConverter 를 추가로 사용해야 할 수 있습니다.
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM); // JSON 처리
    }

    /**
     * 이 컨버터가 "쓰기" 작업을 지원하지 않도록 명시적으로 설정합니다.
     * 요청 데이터를 읽기만 하고, 컨트롤러에서 JSON 데이터를 멀티파트로 변환하지 않습니다.
     *
     * Spring에서 메시지 컨버터는 **읽기(Read)**와 쓰기(Write) 두 가지 작업을 처리할 수 있습니다.
     *
     * 읽기 작업:
     *      클라이언트에서 서버로 들어온 HTTP 요청 데이터를 읽어 Java 객체로 변환.
     *      예: JSON 요청(application/json) → Java 객체
     *
     * 쓰기 작업:
     *      서버에서 클라이언트로 나가는 HTTP 응답 데이터를 변환.
     *      예: Java 객체 → JSON 응답(application/json)
     *
     * 명시적으로 "쓰기" 작업을 비활성화하여 다른 컨버터가 응답 처리를 전담하게 합니다.
     *
     * 위 코드는 이 컨버터가 응답 데이터를 JSON으로 변환하거나 멀티파트 데이터로 쓰는 작업을 수행하지 않도록 설정합니다.
     *
     * 대신, 이 컨버터는 클라이언트 요청의 JSON 데이터를 읽어 Java 객체로 변환하는 데에만 사용됩니다.
     *
     * 이는 명확한 역할 분리와 다른 컨버터와의 충돌 방지를 위해 필요한 설정입니다.
     */
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}
