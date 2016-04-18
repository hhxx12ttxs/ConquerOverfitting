package Backend.ServicesImplementation;

import Backend.Models.MaintenanceByHoursModel;
import Backend.Repositories.MaintenanceByHoursRepository;
import Backend.Services.IMaintenanceByHoursService;
import Backend.Utils.Database.TransactionHandler;
import Backend.Utils.Exceptions.XEntityNotFoundException;
import Backend.Utils.Exceptions.XException;

public class MaintenanceByHoursServiceImplementation implements IMaintenanceByHoursService {
    @Override
    public MaintenanceByHoursModel create(MaintenanceByHoursModel model) throws XException {
        Object returned = TransactionHandler.run(() -> {
            MaintenanceByHoursRepository repository = new MaintenanceByHoursRepository();
            model.setId(0);
            return repository.save(model);
        });
        return (MaintenanceByHoursModel) returned;
    }

    @Override
    public MaintenanceByHoursModel read(int id) throws XException {
        Object returned = TransactionHandler.run(() -> {
            MaintenanceByHoursRepository repository = new MaintenanceByHoursRepository();

            MaintenanceByHoursModel model = (MaintenanceByHoursModel) repository.findById(id);
            if (model == null) throw new XEntityNotFoundException(id, "Programacion por horas");

            return model;
        });
        return (MaintenanceByHoursModel) returned;
    }

    @Override
    public MaintenanceByHoursModel update(int id, MaintenanceByHoursModel model) throws XException {
        Object returned = TransactionHandler.run(() -> {
            MaintenanceByHoursRepository repository = new MaintenanceByHoursRepository();

            model.setId(id);
            return repository.save(model);
        });
        return (MaintenanceByHoursModel) returned;
    }

    @Override
    public void delete(int id) throws XException {
        TransactionHandler.run(() -> {
            MaintenanceByHoursRepository repository = new MaintenanceByHoursRepository();
            repository.delete(id);

            return null;
        });
    }
}

