package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quickbill.data.model.Bill
import com.example.quickbill.ui.QuickBillViewModel
import com.example.quickbill.ui.UiMessage
import com.example.quickbill.ui.components.QuickBillBottomNav
import com.example.quickbill.ui.components.QuickBillHeader
import com.example.quickbill.ui.components.QuickBillTab
import com.example.quickbill.ui.screens.AuthScreen
import com.example.quickbill.ui.screens.BillPreviewScreen
import com.example.quickbill.ui.screens.BillingScreen
import com.example.quickbill.ui.screens.HistoryScreen
import com.example.quickbill.ui.screens.HomeScreen
import com.example.quickbill.ui.screens.SalesScreen
import com.example.quickbill.ui.screens.StockScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    MAIN_TABS,
    BILLING,
    BILL_PREVIEW
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                QuickBillApp()
            }
        }
    }
}

@Composable
fun QuickBillApp(viewModel: QuickBillViewModel = viewModel()) {
    val context = LocalContext.current
    val currentShop by viewModel.currentShop.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val recentBills by viewModel.recentBills.collectAsStateWithLifecycle()
    val allBills by viewModel.allBills.collectAsStateWithLifecycle()
    val todayBills by viewModel.todayBills.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    val previewBill by viewModel.previewBill.collectAsStateWithLifecycle()

    val salesDateBills by viewModel.salesDateBills.collectAsStateWithLifecycle()
    val salesMonthBills by viewModel.salesMonthBills.collectAsStateWithLifecycle()
    val selectedSalesDate by viewModel.selectedSalesDate.collectAsStateWithLifecycle()
    val selectedSalesMonth by viewModel.selectedSalesMonth.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(QuickBillTab.HOME) }
    var screenState by remember { mutableStateOf(ScreenState.MAIN_TABS) }
    var localPreviewBill by remember { mutableStateOf<Bill?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            val text = when (event) {
                is UiMessage.Success -> event.message
                is UiMessage.Error -> "⚠️ ${event.message}"
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    if (currentShop == null) {
        AuthScreen(
            onLogin = { phone, pin, callback ->
                viewModel.login(phone, pin, callback)
            },
            onSignup = { phone, pin, name, callback ->
                viewModel.signup(phone, pin, name, callback)
            },
            language = language
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp), // Tablet / Desktop centered column
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                QuickBillHeader(
                    shop = currentShop,
                    currentLanguage = language,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onLogout = {
                        viewModel.logout()
                        screenState = ScreenState.MAIN_TABS
                        activeTab = QuickBillTab.HOME
                    },
                    onBack = if (screenState != ScreenState.MAIN_TABS) {
                        { screenState = ScreenState.MAIN_TABS }
                    } else null
                )
            },
            bottomBar = {
                // Only show bottom tabs when on MAIN_TABS so billing and preview have clean dedicated bottom actions
                if (screenState == ScreenState.MAIN_TABS) {
                    QuickBillBottomNav(
                        currentTab = activeTab,
                        onTabSelected = { tab ->
                            activeTab = tab
                            screenState = ScreenState.MAIN_TABS
                        },
                        language = language
                    )
                }
            },
            containerColor = AppBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = screenState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { currentScreen ->
                    when (currentScreen) {
                        ScreenState.BILLING -> {
                            BillingScreen(
                                allItems = allItems,
                                currentShop = currentShop,
                                getNextBillNo = { viewModel.getNextBillNo() },
                                onSaveBill = { name, phone, items, onSuccess, onError ->
                                    viewModel.createBill(name, phone, items, onSuccess, onError)
                                },
                                onBillCreated = { bill ->
                                    localPreviewBill = bill
                                    viewModel.setPreviewBill(bill)
                                    screenState = ScreenState.BILL_PREVIEW
                                },
                                language = language
                            )
                        }

                        ScreenState.BILL_PREVIEW -> {
                            BillPreviewScreen(
                                bill = localPreviewBill ?: previewBill,
                                shop = currentShop,
                                onNewBill = {
                                    localPreviewBill = null
                                    viewModel.setPreviewBill(null)
                                    screenState = ScreenState.BILLING
                                },
                                onBack = {
                                    screenState = ScreenState.MAIN_TABS
                                },
                                language = language
                            )
                        }

                        ScreenState.MAIN_TABS -> {
                            when (activeTab) {
                                QuickBillTab.HOME -> {
                                    HomeScreen(
                                        onNavigateToBilling = { screenState = ScreenState.BILLING },
                                        onNavigateToSales = { activeTab = QuickBillTab.SALES },
                                        onNavigateToStock = { activeTab = QuickBillTab.STOCK },
                                        onOpenBillPreview = { bill ->
                                            localPreviewBill = bill
                                            viewModel.setPreviewBill(bill)
                                            screenState = ScreenState.BILL_PREVIEW
                                        },
                                        currentShop = currentShop,
                                        todayTotal = todayTotal,
                                        todayBillCount = todayBills.size,
                                        monthTotal = monthTotal,
                                        lowStockItems = lowStockItems,
                                        recentBills = recentBills,
                                        language = language
                                    )
                                }

                                QuickBillTab.STOCK -> {
                                    StockScreen(
                                        items = allItems,
                                        onSaveItem = { no, name, price, qty, limit, unit, cat, isEdit, orig, cb ->
                                            viewModel.saveItem(no, name, price, qty, limit, unit, cat, isEdit, orig, cb)
                                        },
                                        onDeleteItem = { no -> viewModel.deleteItem(no) },
                                        getNextSuggestedItemNo = { viewModel.getNextSuggestedItemNo() },
                                        language = language
                                    )
                                }

                                QuickBillTab.HISTORY -> {
                                    HistoryScreen(
                                        bills = allBills,
                                        currentShop = currentShop,
                                        onOpenBill = { bill ->
                                            localPreviewBill = bill
                                            viewModel.setPreviewBill(bill)
                                            screenState = ScreenState.BILL_PREVIEW
                                        },
                                        onDeleteBill = { id ->
                                            viewModel.deleteBill(id) {}
                                        },
                                        language = language
                                    )
                                }

                                QuickBillTab.SALES -> {
                                    SalesScreen(
                                        dailyBills = salesDateBills,
                                        monthlyBills = salesMonthBills,
                                        allBills = allBills,
                                        selectedDateKey = selectedSalesDate,
                                        selectedMonthKey = selectedSalesMonth,
                                        onDateSelected = { viewModel.setSalesDate(it) },
                                        onMonthSelected = { viewModel.setSalesMonth(it) },
                                        language = language
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
