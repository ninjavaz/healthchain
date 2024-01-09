package com.healthchain.backend.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.healthchain.backend.model.network.CustomIdentity;
import com.healthchain.backend.model.network.TransactionType;
import com.healthchain.backend.model.util.NetworkProperties;
import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private NetworkProperties networkProps;

    public <T> T evaluateTransaction(TransactionType transactionType,
                                      Class<T> resultClass,
                                      String hospName,
                                      CustomIdentity customIdentity,
                                      Object... args) throws Exception {
        NetworkProperties.HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);

        Identity identity = Identities.newX509Identity(customIdentity.getMspId(), Identities.readX509Certificate(customIdentity.getCredentials().getCertificate()), Identities.readPrivateKey(customIdentity.getCredentials().getPrivateKey()));
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        // Gson instance
        Gson gson = new Gson();
        // Convert object to JSON string
        String[] argsJson = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            argsJson[i] = gson.toJson(args[i]);
        }

        byte[] result = contract.evaluateTransaction(transactionType.getValue(), argsJson);

        System.out.println("TESTOWANIE");
        System.out.println(new String(result));

        return gson.fromJson(new String(result), resultClass);
    }

    public <T> List<T> evaluateTransactionWithList(TransactionType transactionType,
                                                 Class<T> resultClass,
                                                 String hospName,
                                                 CustomIdentity customIdentity, Object... args) throws Exception {
        NetworkProperties.HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);

        Identity identity = Identities.newX509Identity(customIdentity.getMspId(), Identities.readX509Certificate(customIdentity.getCredentials().getCertificate()), Identities.readPrivateKey(customIdentity.getCredentials().getPrivateKey()));
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        Gson gson = new Gson();
        // Convert object to JSON string
        String[] argsJson = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            argsJson[i] = gson.toJson(args[i]);
        }

        byte[] result = contract.evaluateTransaction(transactionType.getValue(), argsJson);

        Type arrayType = TypeToken.getArray(resultClass).getType();
        T[] array = gson.fromJson(new String(result), arrayType);

        return Arrays.asList(array);
    }

    public <T> T submitTransaction(TransactionType transactionType,
                                  Class<T> resultClass,
                                  String hospName, CustomIdentity customIdentity, Object... args) throws Exception {
        NetworkProperties.HospInfo hospInfo = networkProps.getHospInfoByName().get(hospName);

        Gateway.Builder builder = Gateway.createBuilder();

        Identity identity = Identities.newX509Identity(customIdentity.getMspId(), Identities.readX509Certificate(customIdentity.getCredentials().getCertificate()), Identities.readPrivateKey(customIdentity.getCredentials().getPrivateKey()));
        builder.identity(identity)
                .networkConfig(Paths.get(hospInfo.getNetworkConfigPath()))
                .discovery(true);

        // create a gateway connection
        Gateway gateway = builder.connect();
        // get the network and contract
        Network network = gateway.getNetwork(networkProps.getChannel());
        Contract contract = network.getContract(networkProps.getContract());

        // Gson instance
        Gson gson = new Gson();

        // Convert object to JSON string
        String[] argsJson = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            argsJson[i] = gson.toJson(args[i]);
        }

        byte[] result = contract.submitTransaction(transactionType.getValue(), argsJson);

        return gson.fromJson(new String(result), resultClass);
    }
}
