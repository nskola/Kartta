package fi.neskola.kartta.application.di;

import androidx.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import fi.neskola.kartta.viewmodels.RecordListViewModel;

@Module
public abstract class RecordListViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(RecordListViewModel.class)
    public abstract ViewModel bindsRecordListViewModel(RecordListViewModel viewModel);

}
