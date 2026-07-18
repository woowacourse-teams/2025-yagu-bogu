import ComposeApp
import CoreLocation
import KakaoMapsSDK
import UIKit

private enum PlaceMapConstants {
    static let mapViewName = "place-map"
    static let markerLayerID = "place-marker-layer"
    static let markerStyleID = "place-marker-style"
    static let markerPoiID = "place-marker"
    static let zoomLevel = 16
}

final class KakaoPlaceMapView: UIView, MapControllerDelegate {
    private let mapContainer = KMViewContainer()
    private var controller: KMController?
    private var coordinate = CLLocationCoordinate2D(latitude: 37.512150, longitude: 127.071960)
    private var currentAddress = ""
    private var markerPoi: Poi?
    private var markerLayerAdded = false
    private var markerStyleAdded = false
    private var viewAdded = false

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }

    convenience init(address: String, placeName: String, latitude: Double, longitude: Double) {
        self.init(frame: .zero)
        update(address: address, placeName: placeName, latitude: latitude, longitude: longitude)
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupView()
    }

    deinit {
        releaseMap()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        mapContainer.frame = bounds
        resizeMap(to: bounds.size)
    }

    func update(address: String, placeName: String, latitude: Double, longitude: Double) {
        guard
            currentAddress != address ||
            coordinate.latitude != latitude ||
            coordinate.longitude != longitude
        else { return }

        currentAddress = address
        coordinate = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        updateMarker(to: coordinate)
        moveCamera(to: coordinate)
    }

    func releaseMap() {
        controller?.pauseEngine()
        controller?.resetEngine()
        controller = nil
        markerPoi = nil
        markerLayerAdded = false
        markerStyleAdded = false
        viewAdded = false
    }

    @objc func addViews() {
        let position = MapPoint(longitude: coordinate.longitude, latitude: coordinate.latitude)
        let info = MapviewInfo(
            viewName: PlaceMapConstants.mapViewName,
            viewInfoName: "map",
            defaultPosition: position,
            defaultLevel: PlaceMapConstants.zoomLevel
        )
        controller?.addView(info)
    }

    @objc func addViewSucceeded(_ viewName: String, viewInfoName: String) {
        viewAdded = true
        resizeMap(to: bounds.size)
        updateMarker(to: coordinate)
        moveCamera(to: coordinate)
    }

    @objc func addViewFailed(_ viewName: String, viewInfoName: String) {
        viewAdded = false
    }

    @objc func containerDidResized(_ size: CGSize) {
        resizeMap(to: size)
    }

    @objc func authenticationFailed(_ errorCode: Int, desc: String) {
        viewAdded = false
    }

    private func setupView() {
        clipsToBounds = true
        mapContainer.frame = bounds
        mapContainer.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        addSubview(mapContainer)

        controller = KMController(viewContainer: mapContainer)
        controller?.delegate = self
        controller?.prepareEngine()
        controller?.activateEngine()
    }

    private func resizeMap(to size: CGSize) {
        guard viewAdded, let mapView = controller?.getView(PlaceMapConstants.mapViewName) as? KakaoMap else { return }
        mapView.viewRect = CGRect(origin: .zero, size: size)
    }

    private func moveCamera(to coordinate: CLLocationCoordinate2D) {
        guard viewAdded, let mapView = controller?.getView(PlaceMapConstants.mapViewName) as? KakaoMap else { return }
        let target = MapPoint(longitude: coordinate.longitude, latitude: coordinate.latitude)
        let update = CameraUpdate.make(target: target, zoomLevel: PlaceMapConstants.zoomLevel, mapView: mapView)
        mapView.moveCamera(update)
    }

    private func updateMarker(to coordinate: CLLocationCoordinate2D) {
        guard viewAdded, let mapView = controller?.getView(PlaceMapConstants.mapViewName) as? KakaoMap else { return }
        let target = MapPoint(longitude: coordinate.longitude, latitude: coordinate.latitude)

        if markerPoi == nil {
            configureMarkerLayer(on: mapView)
            let layer = mapView.getLabelManager().getLabelLayer(layerID: PlaceMapConstants.markerLayerID)
            let options = PoiOptions(styleID: PlaceMapConstants.markerStyleID, poiID: PlaceMapConstants.markerPoiID)
            options.rank = 0
            markerPoi = layer?.addPoi(option: options, at: target)
        } else {
            markerPoi?.position = target
        }

        markerPoi?.show()
    }

    private func configureMarkerLayer(on mapView: KakaoMap) {
        let manager = mapView.getLabelManager()

        if !markerLayerAdded {
            let layerOption = LabelLayerOptions(
                layerID: PlaceMapConstants.markerLayerID,
                competitionType: .none,
                competitionUnit: .symbolFirst,
                orderType: .rank,
                zOrder: 1000
            )
            _ = manager.addLabelLayer(option: layerOption)
            markerLayerAdded = true
        }

        if !markerStyleAdded {
            let iconStyle = PoiIconStyle(
                symbol: Self.makeMarkerImage(),
                anchorPoint: CGPoint(x: 0.5, y: 1.0)
            )
            let style = PoiStyle(
                styleID: PlaceMapConstants.markerStyleID,
                styles: [PerLevelPoiStyle(iconStyle: iconStyle, level: 0)]
            )
            manager.addPoiStyle(style)
            markerStyleAdded = true
        }
    }

    private static func makeMarkerImage() -> UIImage {
        let size = CGSize(width: 32, height: 40)
        return UIGraphicsImageRenderer(size: size).image { context in
            let bounds = CGRect(origin: .zero, size: size)
            let pinPath = UIBezierPath()

            pinPath.move(to: CGPoint(x: bounds.midX, y: bounds.maxY - 1))
            pinPath.addCurve(
                to: CGPoint(x: 4, y: 15),
                controlPoint1: CGPoint(x: 10, y: 31),
                controlPoint2: CGPoint(x: 4, y: 24)
            )
            pinPath.addArc(
                withCenter: CGPoint(x: bounds.midX, y: 15),
                radius: 12,
                startAngle: .pi,
                endAngle: 0,
                clockwise: true
            )
            pinPath.addCurve(
                to: CGPoint(x: bounds.midX, y: bounds.maxY - 1),
                controlPoint1: CGPoint(x: 28, y: 24),
                controlPoint2: CGPoint(x: 22, y: 31)
            )
            pinPath.close()

            context.cgContext.setShadow(
                offset: CGSize(width: 0, height: 2),
                blur: 4,
                color: UIColor.black.withAlphaComponent(0.25).cgColor
            )
            UIColor(red: 0.13, green: 0.77, blue: 0.37, alpha: 1.0).setFill()
            pinPath.fill()

            UIColor.white.setFill()
            UIBezierPath(ovalIn: CGRect(x: 11, y: 10, width: 10, height: 10)).fill()
        }
    }
}
