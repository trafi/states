struct LanguageSelectionState {

    private let currentLanguage: String
    private let hasRegion: Bool

    private var state: State = .idle
    private enum State {
        case idle
        case settingLanguage(String)
        case loadingConfig
        case resetingLanguage
        case producingOutput
    }

    private var output: LanguageSelectionViewController.OutputType?

    init(currentLanguage: String, hasRegion: Bool) {
        self.currentLanguage = currentLanguage
        self.hasRegion = hasRegion
    }

    // MARK: - Changes

    enum Event {
        case tappedSave(langauge: String)
        case didSetLanguage
        case loadedConfig
        case failedLoadingConfig
        case producedOutput
    }

    static func reduce(state: LanguageSelectionState, event: Event) -> LanguageSelectionState {

        var result = state

        switch event {
        case .tappedSave(let language):
            if result.currentLanguage == language && result.hasRegion {
                result.state = .producingOutput
            } else {
                result.state = .settingLanguage(language)
            }
        case .didSetLanguage:
            switch result.state {
            case .settingLanguage:
                result.state = .loadingConfig
            case .resetingLanguage:
                result.state = .idle
            default:
                break
            }
        case .loadedConfig:
            result.state = .producingOutput
        case .failedLoadingConfig:
            result.state = .resetingLanguage
        case .producedOutput:
            result.state = .idle
        }

        return result
    }

    // MARK: - Queries

    // MARK: UI

    var showActivityPopup: Bool {
        switch state {
        case .idle:
            return false
        case .settingLanguage, .loadingConfig, .resetingLanguage, .producingOutput:
            return true
        }
    }

    // MARK: Data

    var setLanguage: String? {
        switch state {
        case .settingLanguage(let lang):
            return lang
        case .resetingLanguage:
            return currentLanguage
        default:
            return nil
        }
    }

    var loadConfig: ()? {
        guard case .loadingConfig = state else { return nil }
        return ()
    }

    // MARK: Flow

    var produceOutput: LanguagesViewController.OutputType? {
        guard case .producingOutput = state else { return nil }
        return LanguageChanged()
    }
}
