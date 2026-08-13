import AppTrackingTransparency
import ComposeApp
import GoogleMobileAds
import GoogleSignIn
import SwiftUI
import UIKit
import FirebaseCore

@main
struct iOSApp: App {
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

    init() {
        FirebaseApp.configure()
        setupBannerAdProvider()
        setupInterstitialAdProvider()
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
