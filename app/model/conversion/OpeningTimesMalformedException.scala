package model.conversion

case class OpeningTimesMalformedException(
    private val message: String = "",
    private val cause: Throwable = None.orNull
) extends Exception(message, cause)
