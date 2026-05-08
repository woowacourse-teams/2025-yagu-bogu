import GoogleMobileAds
import UIKit

/// 전면 광고의 로드 · 표시 · 재로드 생명주기를 관리하는 코디네이터.
final class InterstitialAdCoordinator: NSObject, FullScreenContentDelegate {
    private var loadedAd: InterstitialAd?
    private var onComplete: (() -> Void)?

    /// 광고를 미리 로드한다. 이미 로드된 광고가 있으면 재로드하지 않음
    func load(adUnitId: String) {
        guard loadedAd == nil else { return }
        InterstitialAd.load(with: adUnitId, request: Request()) { [weak self] ad, error in
            if let error {
                print("[InterstitialAd] 로드 실패: \(error.localizedDescription)")
                return
            }
            self?.loadedAd = ad
            self?.loadedAd?.fullScreenContentDelegate = self
        }
    }

    /// 로드된 광고를 표시. 로드 전이면 광고는 스킵하되 onComplete는 그대로 호출
    func show(from viewController: UIViewController, adUnitId: String, onComplete: @escaping () -> Void) {
        guard let ad = loadedAd else {
            load(adUnitId: adUnitId)
            onComplete()
            return
        }
        self.onComplete = onComplete
        ad.present(from: viewController)
    }

    // MARK: - FullScreenContentDelegate

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        let adUnitId = (ad as? InterstitialAd)?.adUnitID ?? ""
        loadedAd = nil
        load(adUnitId: adUnitId) // 다음 트리거를 위해 즉시 재로드
        onComplete?()
        onComplete = nil
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        let adUnitId = (ad as? InterstitialAd)?.adUnitID ?? ""
        loadedAd = nil
        load(adUnitId: adUnitId)
        onComplete?()
        onComplete = nil
    }
}
