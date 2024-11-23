package com.mobdeve.s14.austria.david.mco2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Text
import com.mobdeve.s14.austria.david.mco2.ui.theme.*

@Composable
fun CurrencyItem(
    name: String,
    ticker: String,
    price: String,
    change: String,
    isPositive: Boolean,
    iconResId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(CurrencyGradient)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Black
            )
            Text(
                text = ticker,
                fontFamily = Gilroy,
                fontSize = 14.sp,
                color = Black
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = price,
                fontFamily = Gilroy,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Black
            )
            Text(
                text = change,
                fontFamily = Gilroy,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isPositive) GreenIndicator else RedIndicator
            )
        }
    }
}
