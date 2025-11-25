package com.market.address;

import java.util.List;

import org.springframework.stereotype.Service;

import com.market.authentication.model.Users;
import com.market.authentication.repository.UsersRepository;
import com.market.order.model.Order;
import com.market.order.repository.OrderRepository;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UsersRepository usersRepository;
    private final OrderRepository orderRepository;

    public AddressService(AddressRepository addressRepository,
                          UsersRepository usersRepository,
                          OrderRepository orderRepository) {
        this.addressRepository = addressRepository;
        this.usersRepository = usersRepository;
        this.orderRepository = orderRepository;
    }

    // ---------------------------------------------------
    // 1️⃣ Add new address
    // ---------------------------------------------------
    public Address addAddress(Long userId, Address address) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        address.setUser(user);
        return addressRepository.save(address);
    }

    // ---------------------------------------------------
    // 2️⃣ Get all addresses of a user
    // ---------------------------------------------------
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserUserId(userId);
    }

    // ---------------------------------------------------
    // 3️⃣ Delete address
    // ---------------------------------------------------
    public String deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
        return "Address deleted successfully";
    }

    // ---------------------------------------------------
    // 4️⃣ Attach Address to Order (when user places order)
    // ---------------------------------------------------
    public Order attachAddressToOrder(Long orderId, Long addressId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Attach address fields to order
       
        // Convert address to string format
        String fullAddress = address.getHouseNo() + ", " +
                             address.getStreet() + ", " +
                             address.getCity() + ", " +
                             address.getState() + ", " +
                             address.getCountry() + " - " +
                             address.getPincode();

        // Save inside order (add a new field addressText if needed)
        // order.setDeliveryAddress(fullAddress);

        return orderRepository.save(order);
    }
}
