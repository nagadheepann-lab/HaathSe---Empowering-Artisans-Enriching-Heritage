package com.example.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MapIntentHelper {

    /**
     * Opens Google Maps with directions to the specified coordinates.
     * If the Google Maps application is not installed or cannot handle the intent,
     * gracefully falls back to launching a browser-compatible Google Maps web URL.
     * Does NOT require continuous background location tracking.
     */
    fun openDirections(
        context: Context,
        latitude: Double,
        longitude: Double,
        destinationName: String
    ) {
        val encodedName = Uri.encode(destinationName)
        
        // 1. Try launching native Google Maps navigation/view URI
        // geo:0,0?q=lat,lng(Label) or https://www.google.com/maps/dir/?api=1&destination=lat,lng
        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&destination_place_id=$encodedName")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Fallback 1: Try geo: URI without package restriction
            try {
                val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedName)")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                // Fallback 2: Launch browser-compatible web Google Maps URL
                try {
                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                    val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "Unable to open maps: $destinationName", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            // General fallback to web browser
            try {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (eBrowser: Exception) {
                Toast.makeText(context, "Location: $destinationName ($latitude, $longitude)", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Opens phone dialer for event organizer contact.
     */
    fun openDialer(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.trim()}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Contact: $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens email client for event inquiries.
     */
    fun openEmail(context: Context, emailAddress: String, subject: String = "Craft Event Inquiry") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailAddress")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Email: $emailAddress", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens official portal or registration web page.
     */
    fun openWebPage(context: Context, url: String) {
        try {
            val webUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Portal: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
