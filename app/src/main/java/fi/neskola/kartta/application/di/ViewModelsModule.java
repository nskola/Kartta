package fi.neskola.kartta.application.di;

import androidx.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import fi.neskola.kartta.viewmodels.KarttaViewModel;
import fi.neskola.kartta.viewmodels.RecordListViewModel;

@Module
public abstract class ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(RecordListViewModel.class)
    public abstract ViewModel bindsRecordListViewModel(RecordListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(KarttaViewModel.class)
    public abstract ViewModel bindsKarttaViewModel(KarttaViewModel viewModel);

}
