package org.ivcode.org.ivcode.knio.io.core.model

/**
 * Data class representing the properties of a security store.
 *
 * @property type The type of the security store. (E.g. JKS, PKCS12)
 * @property path The file path to the security store.
 * @property password The password for accessing the security store.
 */
data class SecurityStoreProperties (
    val type: String,
    val path: String,
    val password: String
)