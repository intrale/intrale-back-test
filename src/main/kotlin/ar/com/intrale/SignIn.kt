package ar.com.intrale

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminGetUserRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.SignUpRequest
import com.google.gson.Gson
import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern
import net.datafaker.Faker
import org.slf4j.Logger
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.log

class SignIn (val config: Config, val faker: Faker, val logger: Logger) : Function {


    override suspend fun execute(textBody:String): Response {

        if (textBody.isEmpty()) return RequestValidationException("Request body not found")

        logger.info("text body: $textBody")
        var body = Gson().fromJson(textBody, ar.com.intrale.SignInRequest::class.java)

        var validation = Validation<ar.com.intrale.SignInRequest> {
            ar.com.intrale.SignInRequest::email required {
                pattern(".+@.+\\..+") hint "El campo email debe tener formato de email. Valor actual: '{value}'"
            }

            ar.com.intrale.SignInRequest::password  required {}
        }

        var validationResult: ValidationResult<Any>
        try {
            validationResult = validation(body)
        } catch (e:Exception){
            e.printStackTrace()
            return RequestValidationException("Request is empty")
        }

        if (validationResult.isValid){

            //val clientIdVal: String = "11pm8ug3bletqjvdl4omvig43u"
            //val secretKey: String = "2i0k4EloPyS2aTsW+YsuxFgTE9vauyCc8bZZeljf"
            //val usernameVal: String = "usuario1"
            val passwordVal: String = "Prueba#1"
            val email: String = body.email

            val attributeType =
                AttributeType {
                    this.name = "email"
                    this.value = email
                }

            val attrs = mutableListOf<AttributeType>()
            attrs.add(attributeType)

            val request =
                AdminGetUserRequest {
                    userPoolId = config.awsCognitoUserPoolId
                    username = email
                }

            try {
                CognitoIdentityProviderClient {
                    region = config.region
                    credentialsProvider
                }.use { identityProviderClient ->
                    var user = identityProviderClient.adminGetUser(request)
                    println("User exists: $user")
                }
            } catch (e:Exception) {
                return ExceptionResponse(e.message ?: "Internal Server Error")
            }

            return Response()
        }

        var errorsMessage: String = ""
        validationResult.errors.forEach {
            errorsMessage += it.dataPath.substring(1) + ' ' + it.message
        }

        return RequestValidationException(errorsMessage)
    }


}