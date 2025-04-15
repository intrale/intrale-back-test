package ar.com.intrale

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminCreateUserRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminGetUserRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminGetUserRequest.Companion.invoke
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest.Companion.invoke
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.UsernameExistsException
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import com.google.gson.Gson
import io.konform.validation.Validation
import io.konform.validation.ValidationResult
import io.konform.validation.jsonschema.minLength
import io.konform.validation.jsonschema.pattern
import net.datafaker.Faker
import org.slf4j.Logger

class SignUp (val config: Config, val faker: Faker, val logger: Logger): Function {

    override suspend fun execute(business: String, function: String, headers: Map<String, String>, textBody:String): Response {

        if (textBody.isEmpty()) return RequestValidationException("Request body not found")

        var body = Gson().fromJson(textBody, ar.com.intrale.SignUpRequest::class.java)

        var validation = Validation<ar.com.intrale.SignUpRequest> {
            ar.com.intrale.SignUpRequest::email  required {
                pattern(".+@.+\\..+") hint "El campo email debe tener formato de email. Valor actual: '{value}'"
            }
        }

        var validationResult: ValidationResult<Any>
        try {
            validationResult = validation(body)
        } catch (e:Exception){
            return RequestValidationException("Request is empty")
        }

        if (validationResult.isValid){

            val email: String = body.email

            val attrs = mutableListOf<AttributeType>()
            attrs.add(AttributeType {
                this.name = "email"
                this.value = email
            })
            attrs.add(AttributeType {
                this.name = "profile"
                this.value = business
            })

            try {
                CognitoIdentityProviderClient {
                    region = config.region
                    credentialsProvider = StaticCredentialsProvider(Credentials(
                        accessKeyId = config.accessKeyId,
                        secretAccessKey = config.secretAccessKey
                    ))
                }.use { identityProviderClient ->
                    try {
                        logger.info("Creamos el usuario")
                        identityProviderClient.adminCreateUser(
                            AdminCreateUserRequest {
                                userPoolId = config.awsCognitoUserPoolId
                                username = email
                                userAttributes = attrs
                            })
                    } catch (e:UsernameExistsException) {
                        // Obtenemos la informacion del usuario
                        logger.info("Obtenemos la informacion del usuario")
                        val user = identityProviderClient.adminGetUser(AdminGetUserRequest {
                            userPoolId = config.awsCognitoUserPoolId
                            username = body.email
                        })
                        val businesses = user.userAttributes?.find { it.name == "profile" }?.value
                        logger.info("businesses: $businesses")
                        if (businesses?.contains(business) == true){
                            return ExceptionResponse(e.message ?: "Internal Server Error")
                        }

                        //Actualizamos la informacion de negocio para el usuario
                        val updateUserAttributesResponse = identityProviderClient.adminUpdateUserAttributes (
                            AdminUpdateUserAttributesRequest {
                                userPoolId = config.awsCognitoUserPoolId
                                username = body.email
                                userAttributes = listOf(
                                    AttributeType {
                                        name = "profile"
                                        value = businesses + "," + business
                                    }
                                )
                            })


                    }
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