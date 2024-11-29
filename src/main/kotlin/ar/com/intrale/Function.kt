package ar.com.intrale

interface Function {
    suspend fun execute(textBody:String): FunctionResponse
}