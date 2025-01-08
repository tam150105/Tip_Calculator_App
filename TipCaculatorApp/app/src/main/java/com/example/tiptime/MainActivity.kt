package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

// Main Activity: Khởi tạo UI và hiển thị giao diện của ứng dụng
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // Cho phép hiển thị màn hình toàn màn hình
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout() // Gọi hàm hiển thị giao diện chính
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
// Hàm này dùng để tạo giao diện xem trước trong môi trường phát triển
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            TipTimeLayout()
        }
    }
}

// Giao diện chính của ứng dụng, nơi người dùng nhập tiền và tính toán tiền tip
@Composable
fun TipTimeLayout() {
    // Khai báo trạng thái cho các giá trị đầu vào
    var amountInput by remember { mutableStateOf("") }
    var amount = amountInput.toDoubleOrNull() ?: 0.0 // Chuyển đổi giá trị nhập vào thành số
    var tipPercentage by remember { mutableStateOf(15.0) } // Tỷ lệ tip mặc định là 15%
    var roundUp by remember { mutableStateOf(false) } // Mặc định không làm tròn tiền tip

    // Tính toán tiền tip và tổng tiền cần trả
    var tip = calculateTip(amount, tipPercentage, roundUp)
    var total = amount + tip

    // Cấu trúc chính của giao diện: Sử dụng Column để xếp các phần tử theo chiều dọc
    Column(
        modifier = Modifier
            .statusBarsPadding() // Đệm cho phần trên cùng của màn hình
            .padding(horizontal = 40.dp) // Đệm các phần tử ngang
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),// Xây dựng thanh cuon khi xoay ngang màn hình
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tiêu đề "Tính tiền tip"
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.2f
            )
        )
        // Trường nhập số tiền hóa đơn
        EditNumberField(
            value = amountInput,
            onValueChange = { amountInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )
        // Hiển thị tỷ lệ phần trăm tip
        Text(
            text = stringResource(R.string.tip_percentage, tipPercentage),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.5f,
                color = Color.Blue
            ),
            modifier = Modifier
                .padding(bottom = 30.dp)
                .padding(top = 16.dp)
        )

        // Phần nhãn và icon cho các mức độ tip (Bad, Good, Amazing)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelWithIcon(label = "Bad", icon = R.drawable.bad_face)
            LabelWithIcon(label = "Good", icon = R.drawable.normal_face)
            LabelWithIcon(label = "Amazing", icon = R.drawable.happy_face)
        }

        // Thanh trượt để chọn tỷ lệ tip
        Slider(
            value = tipPercentage.toFloat(),
            onValueChange = { tipPercentage = it.toDouble() },
            valueRange = 0f..30f,
            steps = 5, // Các mức tip chênh nhau 5%
            modifier = Modifier
                .padding(vertical = 16.dp)
                .height(4.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )

        // Hiển thị các mức phần trăm tip có thể lựa chọn
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            val range = listOf(0, 5, 10, 15, 20, 25, 30)
            range.forEach { percent ->
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize * 1.7f,
                        color = Color.Blue
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom=15.dp)
                )
            }
        }

        // Công cụ cho phép làm tròn số tiền tip
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it },
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Hiển thị số tiền tip và tổng tiền
        Text(
            text = stringResource(R.string.tip_amount, NumberFormat.getCurrencyInstance().format(tip)),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 27.sp,
                textAlign = TextAlign.Start,
                color = Color.Magenta
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.total_amount, NumberFormat.getCurrencyInstance().format(total)),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 27.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )

        // Nút Reset để làm lại từ đầu
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                amountInput = ""
                tipPercentage = 15.0
                roundUp = false
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.reset))
        }
    }
}

// Hàm tính toán số tiền tip dựa trên tỷ lệ phần trăm và yêu cầu làm tròn
private fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean): Double {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        tip = kotlin.math.ceil(tip)
    }
    return tip
}

// Trường nhập tiền hóa đơn
@Composable
fun EditNumberField(modifier: Modifier = Modifier, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = {
            Text(
                text = stringResource(R.string.bill_amount),
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .height(80.dp),
        textStyle = TextStyle(fontSize = 27.sp)
    )
}

// Hàm hiển thị nhãn với icon
@Composable
fun LabelWithIcon(label: String, icon: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        )
    }
}

// Công cụ cho phép làm tròn số tiền tip
@Composable
fun RoundTheTipRow(roundUp: Boolean, onRoundUpChanged: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.round_up_tip),
            style = MaterialTheme.typography.bodyLarge.copy( // Sử dụng kiểu bodyLarge và sửa đổi
                fontWeight = FontWeight.Bold,               // Làm chữ đậm
                fontSize = 22.sp                            // Tăng kích thước chữ (18sp)

            )
        )
        Spacer(modifier = Modifier.weight(1f)) // Đẩy Switch về cuối Row
        Switch(
            checked = roundUp,
            onCheckedChange  = onRoundUpChanged
        )
    }
}
