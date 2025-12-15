package ar.uba.fi.ingsoft1.sistema_comedores.observers;

public interface ComponentObserver {
    void onComponentStockChange();
    void onComponentStatusChange();
}
