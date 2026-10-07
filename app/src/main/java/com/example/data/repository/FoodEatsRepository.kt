package com.example.data.repository

import com.example.data.remote.ApiClient
import com.example.data.remote.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DishOption(
    val id: Long,
    val optionGroup: String,
    val optionName: String,
    val priceAdjustment: Double,
    val isDefault: Boolean = false
)

data class DishItem(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val prepTimeMinutes: Int = 15,
    val dietaryTag: String? = null,
    val popularScore: Int = 90,
    val options: List<DishOption> = emptyList()
)

data class Restaurant(
    val id: Long,
    val name: String,
    val cuisine: String,
    val category: String,
    val distance: String,
    val rating: Double,
    val reviewsCount: Int,
    val deliveryFee: Double,
    val deliveryTime: String,
    val address: String,
    val imageUrl: String,
    val bannerUrl: String,
    val description: String,
    val phone: String,
    val openingHours: String,
    val menu: List<DishItem>
)

data class CartItem(
    val dish: DishItem,
    val merchant: Restaurant,
    val selectedOptions: List<DishOption> = emptyList(),
    var quantity: Int = 1
) {
    val unitPrice: Double
        get() = dish.price + selectedOptions.sumOf { it.priceAdjustment }

    val totalPrice: Double
        get() = unitPrice * quantity
}

data class PlacedOrder(
    val id: Long,
    val customerName: String,
    val merchantName: String,
    val merchantId: Long,
    val itemsSummary: String,
    val itemsCount: Int,
    val subtotal: Double,
    val deliveryFee: Double,
    val total: Double,
    val status: String, // "PLACED", "ACCEPTED", "OUT_FOR_DELIVERY", "DELIVERED"
    val deliveryAddress: String,
    val paymentStatus: String = "PAID",
    val paymentMethod: String = "KHQR / Bakong",
    val orderTime: String = "Just now",
    val driver: String = "Dara K. Express (Auto-assigned)"
)

object FoodEatsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Seed Data mirroring Spring Boot DataInitializer.java and customer.html exactly
    private val SEED_RESTAURANTS: List<Restaurant> = listOf(
        Restaurant(
            id = 1,
            name = "Zando Burger & Grill",
            cuisine = "Gourmet American",
            category = "Burgers",
            distance = "1.2 km",
            rating = 4.8,
            reviewsCount = 320,
            deliveryFee = 1.50,
            deliveryTime = "20 min",
            address = "Street 271, Sangkat Phsar Doeum Thkov, Phnom Penh",
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?w=1200&auto=format&fit=crop&q=80",
            description = "Juicy artisan smash burgers, crispy truffle fries, and premium thick milkshakes crafted daily.",
            phone = "+855 23 888 999",
            openingHours = "10:00 AM - 10:30 PM",
            menu = listOf(
                DishItem(
                    id = 101,
                    name = "Double Truffle Smash Burger",
                    description = "Double black angus beef patties, aged white cheddar, sautéed portobello, and black truffle aioli on a toasted brioche bun.",
                    price = 7.25,
                    category = "Signature Burgers",
                    imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 15,
                    dietaryTag = "Chef Special",
                    popularScore = 99,
                    options = listOf(
                        DishOption(1, "Patty Size", "Double 150g (Standard)", 0.00, true),
                        DishOption(2, "Patty Size", "Triple Monster 225g", 2.25),
                        DishOption(3, "Add-Ons", "Extra Aged Cheddar Slice", 0.75),
                        DishOption(4, "Add-Ons", "Smoked Applewood Bacon", 1.25),
                        DishOption(5, "Add-Ons", "Fried Cage-Free Egg", 0.60)
                    )
                ),
                DishItem(
                    id = 102,
                    name = "Spicy Nashville Crispy Chicken",
                    description = "Buttermilk-brined crispy chicken thigh coated with Nashville cayenne glaze, spicy pickled cucumbers, and house slaw.",
                    price = 5.75,
                    category = "Signature Burgers",
                    imageUrl = "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 12,
                    dietaryTag = "Spicy",
                    popularScore = 94,
                    options = listOf(
                        DishOption(6, "Spice Heat Level", "Mild Heat", 0.00, true),
                        DishOption(7, "Spice Heat Level", "Hot & Crispy", 0.00),
                        DishOption(8, "Spice Heat Level", "Extra Blazing Ghost Pepper", 0.50)
                    )
                ),
                DishItem(
                    id = 103,
                    name = "Parmesan Truffle French Fries",
                    description = "Hand-cut Idaho potato fries tossed with white truffle oil, freshly grated Grana Padano parmesan, and chives.",
                    price = 2.95,
                    category = "Artisan Sides",
                    imageUrl = "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 8,
                    dietaryTag = "Vegetarian"
                ),
                DishItem(
                    id = 104,
                    name = "Salted Caramel Shake",
                    description = "Hand-spun Madagascar vanilla gelato with sea salt caramel ribbon and whipped cream.",
                    price = 3.50,
                    category = "Craft Beverages",
                    imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 5
                )
            )
        ),
        Restaurant(
            id = 2,
            name = "Sakura Sushi & Ramen Bar",
            cuisine = "Japanese Artisan",
            category = "Japanese",
            distance = "2.4 km",
            rating = 4.9,
            reviewsCount = 540,
            deliveryFee = 2.00,
            deliveryTime = "30 min",
            address = "Monivong Blvd, Boeung Keng Kang 1, Phnom Penh",
            imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=1200&auto=format&fit=crop&q=80",
            description = "Authentic Hakata tonkotsu ramen simmered for 18 hours, and fresh seasonal sashimi rolls.",
            phone = "+855 23 777 666",
            openingHours = "11:00 AM - 11:00 PM",
            menu = listOf(
                DishItem(
                    id = 201,
                    name = "Torched Salmon Aburi Roll",
                    description = "8 pieces. Fresh Norwegian salmon lightly torched with house spicy mayo, sweet unagi reduction, and tobiko.",
                    price = 8.50,
                    category = "Specialty Rolls",
                    imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 18,
                    dietaryTag = "Chef Special",
                    popularScore = 98,
                    options = listOf(
                        DishOption(11, "Portion Size", "8 Pieces", 0.00, true),
                        DishOption(12, "Portion Size", "12 Pieces Party Size", 3.80),
                        DishOption(13, "Preparation", "Regular Wasabi", 0.00, true),
                        DishOption(14, "Preparation", "Extra Pickled Ginger & Wasabi", 0.50)
                    )
                ),
                DishItem(
                    id = 202,
                    name = "Signature Tonkotsu Black Ramen",
                    description = "16-hour simmered pork bone broth, handcrafted springy noodles, slow-braised chashu pork, ajitsuke tamago egg, and black garlic oil.",
                    price = 8.00,
                    category = "Hot Broth Ramen",
                    imageUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 15,
                    dietaryTag = "Popular",
                    popularScore = 99,
                    options = listOf(
                        DishOption(15, "Noodle Firmness", "Standard Medium", 0.00, true),
                        DishOption(16, "Noodle Firmness", "Hard / Firm (Katame)", 0.00),
                        DishOption(17, "Extra Toppings", "Extra Braised Chashu (2 slices)", 2.00),
                        DishOption(18, "Extra Toppings", "Extra Ajitsuke Ramen Egg", 1.00)
                    )
                )
            )
        ),
        Restaurant(
            id = 3,
            name = "Khmer Coffee & Patisserie",
            cuisine = "Café & Bakery",
            category = "Coffee & Bakery",
            distance = "0.8 km",
            rating = 4.9,
            reviewsCount = 410,
            deliveryFee = 1.00,
            deliveryTime = "15 min",
            address = "Norodom Blvd, Daun Penh, Phnom Penh",
            imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&auto=format&fit=crop&q=80",
            description = "Traditional slow-drip Mondulkiri iced coffee, French butter croissants, and fresh coconut pandan waffles.",
            phone = "+855 23 555 444",
            openingHours = "07:00 AM - 09:00 PM",
            menu = listOf(
                DishItem(
                    id = 301,
                    name = "Mondulkiri Drip Iced Latte",
                    description = "Double shot Mondulkiri mountain espresso over creamy condensed milk and crushed ice.",
                    price = 2.75,
                    category = "Single Origin Coffee",
                    imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=600&auto=format&fit=crop&q=80",
                    prepTimeMinutes = 5,
                    dietaryTag = "Must Try",
                    popularScore = 97,
                    options = listOf(
                        DishOption(21, "Sweetness Level", "100% Full Sweet", 0.00, true),
                        DishOption(22, "Sweetness Level", "50% Less Sweet", 0.00),
                        DishOption(23, "Sweetness Level", "0% Unsweetened / Black", 0.00),
                        DishOption(24, "Milk Choice", "Fresh Milk", 0.00, true),
                        DishOption(25, "Milk Choice", "Oat Milk (+ $0.60)", 0.60)
                    )
                )
            )
        )
    )

    private val _restaurants = MutableStateFlow<List<Restaurant>>(SEED_RESTAURANTS)
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _orders = MutableStateFlow<List<PlacedOrder>>(
        listOf(
            PlacedOrder(
                id = 101,
                customerName = "Sokha Mean",
                merchantName = "Zando Burger & Grill",
                merchantId = 1,
                itemsSummary = "1x Double Truffle Smash Burger, 1x Parmesan Truffle French Fries",
                itemsCount = 2,
                subtotal = 10.20,
                deliveryFee = 1.50,
                total = 11.70,
                status = "OUT_FOR_DELIVERY",
                deliveryAddress = "Building 42, St. 302, BKK1, Phnom Penh",
                paymentStatus = "PAID_KHQR",
                paymentMethod = "KHQR / Bakong",
                orderTime = "15 mins ago",
                driver = "Dara K. Express (+855 88 776 655)"
            )
        )
    )
    val orders: StateFlow<List<PlacedOrder>> = _orders.asStateFlow()

    private val _serverStatus = MutableStateFlow("Ready")
    val serverStatus: StateFlow<String> = _serverStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadFromBackend() {
        scope.launch {
            _isLoading.value = true
            try {
                val res = ApiClient.merchantApi.getMerchants()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    val dtoList = res.body()!!
                    val mapped = dtoList.map { dto ->
                        // Fetch menu for each merchant
                        val menuItems = try {
                            val menuRes = ApiClient.menuApi.getMenuItems(merchantId = dto.id)
                            if (menuRes.isSuccessful && !menuRes.body().isNullOrEmpty()) {
                                menuRes.body()!!.map { mDto ->
                                    DishItem(
                                        id = mDto.id,
                                        name = mDto.name,
                                        description = mDto.description ?: "",
                                        price = mDto.price,
                                        category = "Specialties",
                                        imageUrl = mDto.imageUrl ?: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600",
                                        prepTimeMinutes = mDto.prepTimeMinutes ?: 15,
                                        dietaryTag = mDto.dietaryTag,
                                        popularScore = mDto.popularScore ?: 90,
                                        options = mDto.options?.map { oDto ->
                                            DishOption(
                                                id = oDto.id,
                                                optionGroup = oDto.optionGroup,
                                                optionName = oDto.optionName,
                                                priceAdjustment = oDto.priceAdjustment,
                                                isDefault = false
                                            )
                                        } ?: emptyList()
                                    )
                                }
                            } else {
                                findSeedMenu(dto.id)
                            }
                        } catch (e: Exception) {
                            findSeedMenu(dto.id)
                        }

                        Restaurant(
                            id = dto.id,
                            name = dto.name,
                            cuisine = dto.cuisineType ?: "Gourmet Specialties",
                            category = dto.cuisineType?.split(" ")?.lastOrNull() ?: "General",
                            distance = "1.5 km",
                            rating = dto.rating ?: 4.8,
                            reviewsCount = dto.reviewsCount ?: 120,
                            deliveryFee = dto.deliveryFee ?: 1.50,
                            deliveryTime = "${dto.deliveryTimeMins ?: 25} min",
                            address = dto.address ?: "Phnom Penh, Cambodia",
                            imageUrl = dto.imageUrl ?: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600",
                            bannerUrl = dto.bannerUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200",
                            description = dto.description ?: "Gourmet dining delivered fresh to your door.",
                            phone = dto.phone ?: "+855 23 000 111",
                            openingHours = dto.openingHours ?: "10:00 AM - 10:00 PM",
                            menu = if (menuItems.isNotEmpty()) menuItems else findSeedMenu(dto.id)
                        )
                    }
                    _restaurants.value = mapped
                    _serverStatus.value = "Connected to Live Database (${mapped.size} restaurants)"
                } else {
                    _serverStatus.value = "Using Offline Seed Data"
                }
            } catch (e: Exception) {
                _serverStatus.value = "Offline Cache Active (${e.message ?: "No Connection"})"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun findSeedMenu(merchantId: Long): List<DishItem> {
        return SEED_RESTAURANTS.find { it.id == merchantId }?.menu ?: SEED_RESTAURANTS[0].menu
    }

    fun addToCart(
        dish: DishItem,
        merchant: Restaurant,
        selectedOptions: List<DishOption> = emptyList()
    ): Boolean {
        val current = _cart.value.toMutableList()

        // Multi-merchant guard: If cart has items from different restaurant, returns false to prompt user
        if (current.isNotEmpty() && current[0].merchant.id != merchant.id) {
            return false
        }

        // Check if item with same options already exists
        val existingIndex = current.indexOfFirst {
            it.dish.id == dish.id && it.selectedOptions == selectedOptions
        }

        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(
                CartItem(
                    dish = dish,
                    merchant = merchant,
                    selectedOptions = selectedOptions,
                    quantity = 1
                )
            )
        }
        _cart.value = current
        return true
    }

    fun replaceCartAndAdd(
        dish: DishItem,
        merchant: Restaurant,
        selectedOptions: List<DishOption> = emptyList()
    ) {
        _cart.value = listOf(
            CartItem(
                dish = dish,
                merchant = merchant,
                selectedOptions = selectedOptions,
                quantity = 1
            )
        )
    }

    fun updateQuantity(cartItemIndex: Int, delta: Int) {
        val current = _cart.value.toMutableList()
        if (cartItemIndex in current.indices) {
            val item = current[cartItemIndex]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(cartItemIndex)
            } else {
                current[cartItemIndex] = item.copy(quantity = newQty)
            }
            _cart.value = current
        }
    }

    fun removeFromCart(cartItemIndex: Int) {
        val current = _cart.value.toMutableList()
        if (cartItemIndex in current.indices) {
            current.removeAt(cartItemIndex)
            _cart.value = current
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun placeOrder(
        customerName: String,
        deliveryAddress: String,
        paymentMethod: String,
        authToken: String? = null
    ): PlacedOrder {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) {
            throw IllegalStateException("Cart is empty")
        }

        val merchant = currentCart.first().merchant
        val subtotal = currentCart.sumOf { it.totalPrice }
        val deliveryFee = merchant.deliveryFee
        val grandTotal = subtotal + deliveryFee
        val itemsSummary = currentCart.joinToString(", ") { "${it.quantity}x ${it.dish.name}" }
        val itemsCount = currentCart.sumOf { it.quantity }

        val newId = 100L + _orders.value.size + 1
        val order = PlacedOrder(
            id = newId,
            customerName = customerName,
            merchantName = merchant.name,
            merchantId = merchant.id,
            itemsSummary = itemsSummary,
            itemsCount = itemsCount,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = grandTotal,
            status = "ACCEPTED",
            deliveryAddress = deliveryAddress,
            paymentStatus = if (paymentMethod.contains("Cash")) "PENDING_CASH" else "PAID_KHQR",
            paymentMethod = paymentMethod,
            orderTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
            driver = "Dara K. Express (Auto-assigned)"
        )

        // Submit to backend asynchronously (works for both authenticated and guest checkout)
        scope.launch {
            try {
                val authHeader = if (!authToken.isNullOrBlank()) "Bearer $authToken" else null
                val requestItems = currentCart.map {
                    com.example.data.remote.dto.CheckoutItemDto(
                        menuItemId = it.dish.id,
                        itemName = it.dish.name,
                        price = it.dish.price,
                        quantity = it.quantity,
                        selectedOptions = it.selectedOptions.joinToString(", ") { opt -> opt.optionName }
                    )
                }
                val resp = ApiClient.orderApi.placeOrder(
                    token = authHeader,
                    request = CheckoutRequestDto(
                        merchantId = merchant.id,
                        deliveryAddress = deliveryAddress,
                        paymentMethod = paymentMethod,
                        items = requestItems
                    )
                )
                if (resp.isSuccessful && resp.body() != null) {
                    android.util.Log.d("FoodEatsRepo", "Successfully submitted order to backend ID=${resp.body()?.id}")
                } else {
                    android.util.Log.w("FoodEatsRepo", "Backend order submission response code=${resp.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("FoodEatsRepo", "Failed to reach backend during order placement: ${e.message}")
            }
        }

        _orders.value = listOf(order) + _orders.value
        clearCart()
        return order
    }

    fun advanceOrderStatus(orderId: Long) {
        val current = _orders.value.toMutableList()
        val index = current.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            val ord = current[index]
            val nextStatus = when (ord.status) {
                "PLACED" -> "ACCEPTED"
                "ACCEPTED" -> "OUT_FOR_DELIVERY"
                "OUT_FOR_DELIVERY" -> "DELIVERED"
                else -> ord.status
            }
            current[index] = ord.copy(status = nextStatus)
            _orders.value = current
        }
    }


    // fetch live orders from spring boot REST API
    fun fetchOrdersFromBackend(authToken: String?){
        if(authToken.isNullOrBlank()) return

        scope.launch{
            try{
                val response = ApiClient.orderApi.getOrders("Bearer $authToken")
                if(response.isSuccessful && !response.body().isNullOrEmpty()){
                    val dtoList = response.body()!!
                    val mapped = dtoList.map { dto ->
                        PlacedOrder(
                            id = dto.id,
                            customerName = dto.customerName ?: "Customer",
                            merchantName = dto.merchantName ?: "Restaurant Kitchen" ,
                            merchantId = dto.merchantId ?: 1L,
                            itemsSummary = dto.itemsSummary ?: "Order Items",
                            itemsCount = 1,
                            subtotal = dto.totalAmount,
                            deliveryFee = dto.deliveryFee,
                            total = dto.totalAmount,
                            status = dto.status,
                            deliveryAddress = dto.deliveryAddress ?: "Phnom Penh",
                            paymentStatus = dto.paymentStatus ?: "PAID" ,
                            paymentMethod = dto.paymentMethod ?: "KHQR / Bakong" ,
                            orderTime = dto.createdAt ?: "Just now",
                            driver = "Assigning Driver ..."
                        )
                    }
                    _orders.value = mapped
                }
            }catch(e: Exception){
                //ignore network glitches during background polling
            }
        }
    }

    //cancel an order if still pending or placed
    fun cancelOrder(orderId: Long, authToken: String?){
        //Instant local update for smooth UI
        val current = _orders.value.toMutableList()
        val index = current.indexOfFirst{it.id == orderId}
        if(index >= 0){

        current[index] = current[index].copy(status = "CANCELLED")
        _orders.value = current
        }

        // Notify spring boot
        if(!authToken.isNullOrBlank()){

        scope.launch{
            try{
                ApiClient.orderApi.updateOrderStatus(
                    token = "Bearer $authToken",
                    id = orderId,
                    body = mapOf("status" to "CANCELLED")
                )
            }catch(e: Exception){
                //it will handle locally
            }
        }}
    }

    // Driver active delivery job state
    private val _activeDriverJob = MutableStateFlow<PlacedOrder?>(null)
    val activeDriverJob: StateFlow<PlacedOrder?> = _activeDriverJob.asStateFlow()

    // Accept a job from the board
    fun acceptJobAsDriver(order: PlacedOrder, authToken: String?) {
        val updated = order.copy(
            status = "ACCEPTED",
            driver = "K'noeun Express"
        )
        _activeDriverJob.value = updated

        // Notify Spring Boot backend
        if (!authToken.isNullOrBlank()) {
            scope.launch {
                try {
                    ApiClient.driverApi.acceptDeliveryJob("Bearer $authToken", order.id)
                } catch (e: Exception) {
                    android.util.Log.d("DriverJob", "Local fallback accepted")
                }
            }
        }
    }

    // Step 2: Confirm Picked Up from Kitchen (sets status to OUT_FOR_DELIVERY)
    fun confirmFoodPickedUp(orderId: Long, authToken: String?) {
        _activeDriverJob.value = _activeDriverJob.value?.copy(status = "OUT_FOR_DELIVERY")
        advanceOrderStatus(orderId)

        if (!authToken.isNullOrBlank()) {
            scope.launch {
                try {
                    ApiClient.driverApi.updateDeliveryStatus(
                        token = "Bearer $authToken",
                        id = orderId,
                        body = mapOf("status" to "OUT_FOR_DELIVERY")
                    )
                } catch (e: Exception) {}
            }
        }
    }

    // Step 3: Complete Delivery to Customer (sets status to DELIVERED)
    fun completeDelivery(orderId: Long, authToken: String?) {
        _activeDriverJob.value = null // Clears the active job
        advanceOrderStatus(orderId)

        if (!authToken.isNullOrBlank()) {
            scope.launch {
                try {
                    ApiClient.driverApi.updateDeliveryStatus(
                        token = "Bearer $authToken",
                        id = orderId,
                        body = mapOf("status" to "DELIVERED")
                    )
                } catch (e: Exception) {}
            }
        }
    }
}
