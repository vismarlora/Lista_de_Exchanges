package com.example.listadeexchanges

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listadeexchanges.ui.theme.ListaDeExchangesTheme
import com.example.listadeexchanges.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import retrofit2.HttpException
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaDeExchangesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ExchangeListScreen()
                }
            }
        }
    }
}

@Composable
fun ExchangeListScreen(
    viewModel: ExchangeViewModel = hiltViewModel()
) {

    val state = viewModel.state.value

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(state.exchanges) { exchange ->
                ExchangeItem(exchange = exchange, {})
            }
        }

        if (state.isLoading)
            CircularProgressIndicator()
    }
}

@Composable
fun ExchangeItem(
    exchange: ExchangeDto,
    onClick: (ExchangeDto) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick(exchange) }
        .padding(20.dp)
    ) {
        Text(
            text = exchange.name,
            style = MaterialTheme.typography.body1,
            overflow = TextOverflow.Ellipsis,

            )

        Text(
            text = exchange.description.toString(),
            style = MaterialTheme.typography.body1,
            overflow = TextOverflow.Ellipsis,

            )


        Text(
            text = if(exchange.active) "Activa" else "Inactiva",
            style = MaterialTheme.typography.body2,
            color = if(exchange.active) Color.Green else Color.Red ,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Text(
            text = exchange.last_updated,
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExchangeScreen(viewModel: ExchangeViewModel = hiltViewModel()) {
    //val coin = viewModel.coin.value

    Column(modifier = Modifier.fillMaxSize()) {
        /* Text(text = coin.name)
         Text(text = coin.symbol)*/
    }

}

//RUTA: data/remote/dto
data class ExchangeDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val active: Boolean = false,
    val last_updated: String = ""
)

//RUTA: data/remote
interface ExchangeApi {
    @GET("/v1/exchanges")
    suspend fun getExchanges(): List<ExchangeDto>

    @GET("/v1/exchanges/{exchangeId}")
    suspend fun getExchange(@Path("exchangeId") exchangeId: String): ExchangeDto
}

class ExchangesRepository @Inject constructor(
    private val api: ExchangeApi
) {
    fun getExchanges(): Flow<Resource<List<ExchangeDto>>> = flow {
        try {
            emit(Resource.Loading()) //indicar que estamos cargando

            val exchanges =
                api.getExchanges() //descarga las monedas de internet, se supone quedemora algo

            emit(Resource.Success(exchanges)) //indicar que se cargo correctamente y pasarle las monedas
        } catch (e: HttpException) {
            //error general HTTP
            emit(Resource.Error(e.message ?: "Error HTTP GENERAL"))
        } catch (e: IOException) {
            //debe verificar tu conexion a internet
            emit(Resource.Error(e.message ?: "verificar tu conexion a internet"))
        }
    }
}

data class ExchangeListState(
    val isLoading: Boolean = false,
    val exchanges: List<ExchangeDto> = emptyList(),
    val error: String = ""
)