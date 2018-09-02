package kamilmilik.gps_tracker.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kamilmilik.gps_tracker.R

/**
 * Created by kamil on 28.07.2018.
 */
object FirebaseAuthExceptions {
    fun translate(context: Context, task: Task<AuthResult>) {
        task.exception?.let { taskException ->
            try {
                throw taskException
            } catch (e: FirebaseAuthException) {
                when (e.errorCode) {
                    "ERROR_INVALID_CUSTOM_TOKEN" -> {
                        Toast.makeText(context, context.getString(R.string.errorInvalidCustomToken), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_CUSTOM_TOKEN_MISMATCH" -> {
                        Toast.makeText(context, context.getString(R.string.errorCustomTokenMismatch), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_INVALID_CREDENTIAL" -> {
                        Toast.makeText(context, context.getString(R.string.errorInvalidCredential), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_INVALID_EMAIL" -> {
                        Toast.makeText(context, context.getString(R.string.errorInvalidEmail), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_WRONG_PASSWORD" -> {
                        Toast.makeText(context, context.getString(R.string.errorWrongPassword), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_USER_MISMATCH" -> {
                        Toast.makeText(context, context.getString(R.string.errorUserMismatch), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_REQUIRES_RECENT_LOGIN" -> {
                        Toast.makeText(context, context.getString(R.string.errorRequestRecentLogin), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                        Toast.makeText(context, context.getString(R.string.errorAccountExistsWithDifferentCredential), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        Toast.makeText(context, context.getString(R.string.errorEmailAlreadyInUse), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> {
                        Toast.makeText(context, context.getString(R.string.errorCredentialAlreadyInUse), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_USER_DISABLED" -> {
                        Toast.makeText(context, context.getString(R.string.errorUserDisabled), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_USER_TOKEN_EXPIRED" -> {
                        Toast.makeText(context, context.getString(R.string.errorUserTokenExpired), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_USER_NOT_FOUND" -> {
                        Toast.makeText(context, context.getString(R.string.errorUserNotFound), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_INVALID_USER_TOKEN" -> {
                        Toast.makeText(context, context.getString(R.string.errorInvalidUserToken), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_OPERATION_NOT_ALLOWED" -> {
                        Toast.makeText(context, context.getString(R.string.errorOperationNotAllowed), Toast.LENGTH_LONG).show()
                    }
                    "ERROR_WEAK_PASSWORD" -> {
                        Toast.makeText(context, context.getString(R.string.errorWeakPassword), Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }
}