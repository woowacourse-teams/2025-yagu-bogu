import AppTrackingTransparency
import ComposeApp
import GoogleMobileAds
import GoogleSignIn
import SwiftUI
import UIKit
import FirebaseCore

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        // Firebase 먼저 초기화
        FirebaseApp.configure()

        // Koin 초기화 (GeofenceObserver createdAtStart=true 이므로 여기서 실행되어야 필요)
        KoinInitializerKt.setup(
            googleSignInDelegate: SwiftGoogleSignInDelegate(),
            appleSignInDelegate: SwiftAppleSignInDelegate()
        )

        if launchOptions?[.location] != nil {
            print("앱이 지오펜스 이벤트로 재실행됨")
        }

        return true
    }
}


@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        setupBannerAdProvider()
        setupInterstitialAdProvider()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
            .onOpenURL { url in
                GIDSignIn.sharedInstance.handle(url)
            }
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                // 앱이 완전히 활성화된 시점에 ATT 팝업 요청
                requestATTAndInitAds()
            }
        }
    }

    /// ATT 요청 및 AdMob 초기화 로직
    private func requestATTAndInitAds() {
        // iOS 14 이상에서만 실행 (그 이외 버전은 바로 초기화)
        if #available(iOS 14, *) {
            ATTrackingManager.requestTrackingAuthorization { status in
                // 사용자가 허용하든 거부하든 AdMob은 초기화해야 함
                DispatchQueue.main.async {
                    MobileAds.shared.start(completionHandler: nil)
                    print("ATT Status: \(status.rawValue)")
                }
            }
        } else {
            MobileAds.shared.start(completionHandler: nil)
        }
    }

    // Kotlin BannerAdProvider에 GADBannerView 생성 팩토리 주입
    private func setupBannerAdProvider() {
        BannerAdProvider.shared.create = { adUnitId, heightPx in
            let gadAdSize: GoogleMobileAds.AdSize
            switch heightPx {
            case 100: gadAdSize = GoogleMobileAds.AdSizeLargeBanner
            case 250: gadAdSize = GoogleMobileAds.AdSizeMediumRectangle
            default: gadAdSize = GoogleMobileAds.AdSizeBanner
            }
            let bannerView = BannerView(adSize: gadAdSize)
            bannerView.adUnitID = adUnitId
            if let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .flatMap({ $0.windows })
                .first(where: { $0.isKeyWindow })?.rootViewController
            {
                bannerView.rootViewController = rootVC
            }
            bannerView.load(Request())
            return bannerView
        }
    }

    // Kotlin InterstitialAdProvider에 preload/show 클로저 주입
    private func setupInterstitialAdProvider() {
        let coordinator = InterstitialAdCoordinator()

        InterstitialAdProvider.shared.preload = { adUnitId in
            coordinator.load(adUnitId: adUnitId)
        }

        InterstitialAdProvider.shared.show = { adUnitId, onComplete in
            guard
                let rootVC = UIApplication.shared.connectedScenes
                    .compactMap({ $0 as? UIWindowScene })
                    .flatMap({ $0.windows })
                    .first(where: { $0.isKeyWindow })?.rootViewController
            else {
                onComplete()
                return
            }
            coordinator.show(from: rootVC, adUnitId: adUnitId, onComplete: { _ = onComplete() })
        }
    }
}
