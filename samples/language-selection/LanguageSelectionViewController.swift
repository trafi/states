private typealias S = LanguageSelectionState
private typealias E = LanguageSelectionState.Event
private typealias Feedback = (Driver<S>) -> Signal<E>

extension LanguageSelectionState {
    init() { self.init(currentLanguage: L10n.currentLanguage, hasRegion: API.Region.isSelected) }
}

class LanguageSelectionViewController: ModalTableController<LanguageOption>, IO {

    typealias InputType = LanguageSelectionState
    typealias OutputType = LanguageChanged

    // MARK: -

    override func viewDidLoad() {
        super.viewDidLoad()

        setupModal()

        Driver.system(initialState: input,
                      reduce: S.reduce,
                      feedback: [bindUI()] + bindData() + bindFlow())
            .drive()
            .disposed(by: rx_disposeBag)
    }

    private func setupModal() {

        navigationBarLayout = NavigationBarLayout(title: TextData(L10n.selectYourLanguageTitle()))
        confirmationTitle = L10n.actionSave()
        cancellationTitle = nil

        let languages = LanguageOption.available()
        let current = languages.index { $0.language == L10n.currentLanguage }

        data = ModalData(style: .fullscreen)
        set(cells: languages, selection: current)
    }

    // MARK: - Feedbacks
    // MARK: UI

    private func bindUI() -> Feedback {
        return bind(self) { `self`, state in

            let activityPopup = ActivityPopupLayout()
            let showActivityPopup = state
                .query { $0.showActivityPopup }
                .driveNext { activityPopup.display(in: self, isVisible: $0) }

            let tappedSaveSubject = PublishSubject<String>()
            self.didCompleteSelection = { tappedSaveSubject.onNext($0.language) }
            let tappedSave = tappedSaveSubject.map(E.tappedSave).asSignal(onErrorSignalWith: .empty())

            return Bindings(subscriptions: [showActivityPopup], mutations: [tappedSave])
        }
    }

    // MARK: Data

    private func bindData() -> [Feedback] {
        return [
            setLanguage(),
            loadConfig(),
        ]
    }

    private func setLanguage() -> Feedback {
        return react(query: { $0.setLanguage }) {
            L10n.set(language: $0)
            return .just(E.didSetLanguage)
        }
    }

    private func loadConfig() -> Feedback {
        return react(query: { $0.loadConfig }) {
            API.V1AppConfigGet().request()
                .map { _ in E.loadedConfig }
                .asSignal(onErrorJustReturn: E.failedLoadingConfig)
        }
    }

    // MARK: Flow

    private func bindFlow() -> [Feedback] {
        return [
            produceOutput(),
        ]
    }

    private func produceOutput() -> Feedback {
        return react(query: { $0.produceOutput }) { [unowned self] in
            self.produce($0)
            return .just(E.producedOutput)
        }
    }
}
