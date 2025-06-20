package com.example.voyago

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.voyago.components.TopAppBar
import com.example.voyago.pages.ChatScreen
import com.example.voyago.pages.CreateNewTravelScreen
import com.example.voyago.pages.EditProfileScreen
import com.example.voyago.pages.EditTravelScreen
import com.example.voyago.pages.FavoriteScreen
import com.example.voyago.pages.MyTravelProposalScreen
import com.example.voyago.pages.NotificationsScreen
import com.example.voyago.pages.OtherProfileScreen
import com.example.voyago.pages.ProfileScreen
import com.example.voyago.pages.ReviewPage
import com.example.voyago.pages.TravelProposalScreen2
import com.example.voyago.pages.TripPageController
import com.example.voyago.ui.theme.DarkGreen20
import com.example.voyago.ui.theme.LighGreen20
import com.example.voyago.ui.theme.VoyagoTheme
import com.example.voyago.utils.SupabaseManager
import com.example.voyago.utils.UserStatusManager
import com.example.voyago.utils.createChatWithInitialMessages
import com.example.voyago.utils.initializeChatsOnce
import com.example.voyago.utils.populate
import com.example.voyago.utils.populateReviews
import com.example.voyago.viewModels.PastTravelViewModel
import com.example.voyago.viewModels.ProfileViewModel
import com.example.voyago.viewModels.TravelViewModel
import com.example.voyago.viewModels.TravelViewModels
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch


// 1629230648 id marco -> travel 3 - 4
// 1619771197 id lore -> 1 - 2
// 1273795543 id gianlu -> 5 - 7
// 1824944549 id matte -> 6 - 8


val profileViewModel = ProfileViewModel()
class MainActivity : AppCompatActivity() {
    private val createTravelModel: TravelViewModel by viewModels()
    private lateinit var userStatusManager: UserStatusManager


    override fun onCreate(savedInstanceState: Bundle?) {

        SupabaseManager.initialize(this)

        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)
        FirebaseApp.initializeApp(this)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val uid = currentUser.uid
                    val db = FirebaseFirestore.getInstance()
                    db.collection("profiles").document(uid)
                        .update("token", token)
                        .addOnSuccessListener {
                            Log.d("VOYAGO_DEBUG", "✅ Token salvato nel profilo Firebase")
                        }
                        .addOnFailureListener { e ->
                            Log.e("VOYAGO_DEBUG", "❌ Errore salvataggio token", e)
                        }
                } else {
                    Log.e("VOYAGO_DEBUG", "❌ Errore recupero token: ${task.exception}")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permission = arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            requestPermissions(permission, 112)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES


        // (Facoltativo) Rende l’app full immersive
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION



        userStatusManager = UserStatusManager(currentUser.uid)
        lifecycle.addObserver(userStatusManager)

        setContent {
            VoyagoTheme {
                MainScreenWithBottomNav(
                    createTravelModel = createTravelModel,
                )
            }
        }
    }

}
@Composable
fun MainScreenWithBottomNav(
    createTravelModel: TravelViewModel,
) {
    val fireBaseUser = FirebaseAuth.getInstance().currentUser
    LaunchedEffect(fireBaseUser) {
        fireBaseUser?.uid?.let { uid ->
            profileViewModel.loadProfile(uid)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val screenType = getScreenType(currentRoute)
    var isChat by remember { mutableStateOf(false) }

    if (screenType == ScreenType.Fullscreen || isChat == true) {
        NavigationGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize() ,
            createTravelModel = createTravelModel,
            onChange = {isChat = !isChat}
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize() ,
            topBar = {
                when (screenType) {
                    ScreenType.EditProfile, ScreenType.OtherProfile, ScreenType.Reviewscreen-> TopAppBar(
                        screen = "profile",
                        finish = { navController.popBackStack() },
                    )
                    ScreenType.EditTrip -> TopAppBar(
                        screen = "editTrip",
                        finish = { navController.popBackStack() }
                    )
                    else -> null
                }
            },
            bottomBar = {
                when (screenType) {
                    ScreenType.BottomNav, ScreenType.Profile -> BottomNavigationBar(navController)
                    else -> null
                }
            },
            floatingActionButton = {
                if (screenType == ScreenType.Profile) {
                    FloatingActionButton(onClick = {
                        navController.navigate("editProfile/${profileViewModel.id}")
                    },
                        containerColor = DarkGreen20,
                        contentColor = Color.White,
                        ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            }
        ) { innerPadding ->
            NavigationGraph(
                navController = navController,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                createTravelModel = createTravelModel,
                onChange = {isChat = !isChat}
                )
        }
    }
}
enum class ScreenType {
    EditProfile,
    Profile,
    EditTrip,
    OtherProfile,
    BottomNav,
    Fullscreen,
    Reviewscreen,
    Unknown
}
private fun getScreenType(route: String?): ScreenType = when {
    route == null -> ScreenType.Unknown
    //route.startsWith("trip_page") -> ScreenType.Trip
    route.startsWith("editProfile") -> ScreenType.EditProfile
    route == BottomNavItem.Profile.route -> ScreenType.Profile
    route.startsWith("editTrip") -> ScreenType.EditTrip
    route.startsWith("profile") -> ScreenType.OtherProfile
    //route == "createTravel" -> ScreenType.CreateTravel
    route.startsWith( "review_page") -> ScreenType.Reviewscreen
    route == "login" || route == "splash" || route == "createTravel" || route == "trip_page" -> ScreenType.Fullscreen

    route.startsWith("mytrip") ||
            route in listOf(
        BottomNavItem.TravelList.route,
        BottomNavItem.Chat.route,
        BottomNavItem.Favorites.route,
        BottomNavItem.Profile.route
    ) -> ScreenType.BottomNav

    else -> ScreenType.Unknown
}

sealed class BottomNavItem(val route: String, val label: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector) {
    object TravelList : BottomNavItem("travel_list", "Explore", Icons.Filled.Search, Icons.Outlined.Search)
    object Chat : BottomNavItem("chat", "Chat", Icons.Filled.Send, Icons.Outlined.Send)
    object MyProposals : BottomNavItem("mytrip", "Trip", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
    object Favorites : BottomNavItem("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.TravelList,
        BottomNavItem.Chat,
        BottomNavItem.MyProposals,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        tonalElevation = 4.dp,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            Divider(color = Color.LightGray, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute?.startsWith(item.route) == true

                    val routeToNavigate = if (item is BottomNavItem.MyProposals) {
                        "mytrip/Booked" // oppure "mytrip/Organized" se preferisci
                    } else {
                        item.route
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                navController.navigate(routeToNavigate) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                            maxLines = 1
                        )
                    }
                }

            }
        }

    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    createTravelModel: TravelViewModel,
    onChange: () -> Unit
) {
    NavHost(navController, startDestination = BottomNavItem.TravelList.route, modifier = modifier) {
        composable(BottomNavItem.TravelList.route) {
            TravelProposalScreen2(navController = navController, userId = profileViewModel.id)
        }
        composable(BottomNavItem.Chat.route) {
            ChatScreen(
                onChange = onChange
            )
        }
        composable("mytrip/{initialTab}") { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getString("initialTab") ?: "Booked"
            MyTravelProposalScreen(userId = profileViewModel.id, navController=navController, initialTab=initialTab)
        }
        composable(BottomNavItem.Favorites.route) {
            FavoriteScreen(userId = profileViewModel.id, navController = navController)
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                addTravel = { navController.navigate("createTravel") },
                navController = navController
            )
        }

        composable("profile/{profileId}") { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")?.toIntOrNull() ?: -1
            OtherProfileScreen(navController = navController, profileId = profileId)
        }
        composable("trip_page/{tripId}/{isPersonal}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: -1
            val isPersonal = backStackEntry.arguments?.getString("isPersonal")?.toBooleanStrictOrNull() ?: false
            TripPageController(
                tripId = tripId,
                isPersonal = isPersonal,
                onBack = { navController.popBackStack() },
                gotoProfile = { id -> navController.navigate("profile/$id") },
                modifier = Modifier,
                navController = navController
            )
        }


        composable("editProfile/{profileId}") {
            EditProfileScreen(profileViewModel)
        }
        composable("editTrip/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: return@composable
            EditTravelScreen(tripId = tripId)
        }
        composable("createTravel") {
            CreateNewTravelScreen(
                createTravelModel,
                navController)
        }
        composable("review_page/{id}/{isUser}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            val isUser = backStackEntry.arguments?.getString("isUser")?.toBooleanStrictOrNull()?: false
            ReviewPage(id = id,isUser= isUser)
        }
        composable("notification") {
            NotificationsScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                userId = profileViewModel.id.toString()
            )
        }
    }
}
