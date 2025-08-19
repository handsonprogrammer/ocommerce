package com.ocommerce.services.order.controller;

import com.ocommerce.services.order.dto.CreateOrderRequestDTO;
import com.ocommerce.services.order.dto.OrderResponseDTO;
import com.ocommerce.services.order.mapper.OrderMapper;
import com.ocommerce.services.order.service.OrderService;
import com.ocommerce.services.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Order Management", description = "APIs for managing customer orders, including creation, retrieval, and cancellation of orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order from the user's current cart contents. The cart must contain at least one item and all items must be available. Requires valid shipping and billing addresses. The cart will be cleared upon successful order creation.",
        operationId = "createOrder"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponseDTO.class),
                examples = @ExampleObject(
                    name = "Successful order creation",
                    value = """
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "orderNumber": "ORD-2024-001234",
                        "status": "PENDING",
                        "totalAmount": 249.98,
                        "currency": "USD",
                        "items": [
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440001",
                                "productId": "550e8400-e29b-41d4-a716-446655440002",
                                "productName": "Wireless Headphones",
                                "quantity": 2,
                                "unitPrice": 99.99,
                                "totalPrice": 199.98
                            }
                        ],
                        "shippingAddress": {
                            "id": "550e8400-e29b-41d4-a716-446655440003",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001",
                            "country": "USA"
                        },
                        "billingAddress": {
                            "id": "550e8400-e29b-41d4-a716-446655440004",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001",
                            "country": "USA"
                        },
                        "createdAt": "2024-01-15T10:30:00Z",
                        "updatedAt": "2024-01-15T10:30:00Z"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid order request - validation errors or business rule violations",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Empty cart",
                        value = """
                        {
                            "error": "EMPTY_CART",
                            "message": "Cannot create order from empty cart"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Invalid address",
                        value = """
                        {
                            "error": "INVALID_ADDRESS",
                            "message": "Shipping or billing address not found or not accessible"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Product unavailable",
                        value = """
                        {
                            "error": "PRODUCT_UNAVAILABLE",
                            "message": "One or more products in cart are no longer available",
                            "details": ["Product 'Wireless Headphones' is out of stock"]
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found for the authenticated user",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "CART_NOT_FOUND",
                        "message": "No active cart found for user"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Order creation request containing shipping and billing address IDs",
                required = true,
                schema = @Schema(implementation = CreateOrderRequestDTO.class)
            )
            @Valid @RequestBody CreateOrderRequestDTO requestDTO) {
        var order = orderService.createOrderFromCart(
            user.getId(),
            requestDTO.getShippingAddressId(),
            requestDTO.getBillingAddressId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderMapper.toOrderResponseDTO(order));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Retrieve order details",
        description = "Fetches detailed information about a specific order including all order items, addresses, and current status. Only orders belonging to the authenticated user can be accessed.",
        operationId = "getOrderById"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order details retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponseDTO.class),
                examples = @ExampleObject(
                    name = "Order details",
                    value = """
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "orderNumber": "ORD-2024-001234",
                        "status": "CONFIRMED",
                        "totalAmount": 249.98,
                        "currency": "USD",
                        "items": [
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440001",
                                "productId": "550e8400-e29b-41d4-a716-446655440002",
                                "productName": "Wireless Headphones",
                                "quantity": 2,
                                "unitPrice": 99.99,
                                "totalPrice": 199.98
                            }
                        ],
                        "shippingAddress": {
                            "id": "550e8400-e29b-41d4-a716-446655440003",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001",
                            "country": "USA"
                        },
                        "billingAddress": {
                            "id": "550e8400-e29b-41d4-a716-446655440004",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001",
                            "country": "USA"
                        },
                        "createdAt": "2024-01-15T10:30:00Z",
                        "updatedAt": "2024-01-15T10:35:00Z"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not accessible by the authenticated user",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "ORDER_NOT_FOUND",
                        "message": "Order with specified ID not found or not accessible"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponseDTO> getOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Unique identifier of the order",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID id) {
        return orderService.getOrderById(id, user.getId())
            .map(orderMapper::toOrderResponseDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
        summary = "Retrieve user orders",
        description = "Fetches a paginated list of orders for the authenticated user. Orders are returned in descending order by creation date (most recent first). Supports pagination to handle large order histories efficiently.",
        operationId = "getUserOrders"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "Paginated orders",
                    value = """
                    {
                        "content": [
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440000",
                                "orderNumber": "ORD-2024-001234",
                                "status": "DELIVERED",
                                "totalAmount": 249.98,
                                "currency": "USD",
                                "itemCount": 2,
                                "createdAt": "2024-01-15T10:30:00Z",
                                "updatedAt": "2024-01-18T14:20:00Z"
                            },
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440001",
                                "orderNumber": "ORD-2024-001233",
                                "status": "SHIPPED",
                                "totalAmount": 89.99,
                                "currency": "USD",
                                "itemCount": 1,
                                "createdAt": "2024-01-10T15:45:00Z",
                                "updatedAt": "2024-01-12T09:15:00Z"
                            }
                        ],
                        "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                                "sorted": true,
                                "unsorted": false
                            }
                        },
                        "totalElements": 25,
                        "totalPages": 3,
                        "last": false,
                        "first": true,
                        "numberOfElements": 10
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<Page<OrderResponseDTO>> getUserOrders(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Page number (0-based)",
                example = "0",
                schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                description = "Number of orders per page",
                example = "10",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> orders = orderService.getUserOrders(user.getId(), pageable)
            .map(orderMapper::toOrderResponseDTO);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/cancel")
    @Operation(
        summary = "Cancel an order",
        description = "Cancels an existing order if it's in a cancellable state (PENDING or CONFIRMED). Once cancelled, the order cannot be processed further and any reserved inventory will be released. Orders that are already shipped or delivered cannot be cancelled.",
        operationId = "cancelOrder"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponseDTO.class),
                examples = @ExampleObject(
                    name = "Cancelled order",
                    value = """
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "orderNumber": "ORD-2024-001234",
                        "status": "CANCELLED",
                        "totalAmount": 249.98,
                        "currency": "USD",
                        "items": [
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440001",
                                "productId": "550e8400-e29b-41d4-a716-446655440002",
                                "productName": "Wireless Headphones",
                                "quantity": 2,
                                "unitPrice": 99.99,
                                "totalPrice": 199.98
                            }
                        ],
                        "shippingAddress": {
                            "id": "550e8400-e29b-41d4-a716-446655440003",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001",
                            "country": "USA"
                        },
                        "billingAddress": {
                            "id": "550e8400-e29b-41d4-a716-446655440004",
                            "street": "123 Main St",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001",
                            "country": "USA"
                        },
                        "createdAt": "2024-01-15T10:30:00Z",
                        "updatedAt": "2024-01-15T11:45:00Z",
                        "cancelledAt": "2024-01-15T11:45:00Z"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Order cannot be cancelled - invalid status",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Already cancelled",
                        value = """
                        {
                            "error": "ORDER_ALREADY_CANCELLED",
                            "message": "Order is already cancelled"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Cannot cancel shipped order",
                        value = """
                        {
                            "error": "ORDER_NOT_CANCELLABLE",
                            "message": "Order cannot be cancelled as it has already been shipped"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not accessible by the authenticated user",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "ORDER_NOT_FOUND",
                        "message": "Order with specified ID not found or not accessible"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Unique identifier of the order to cancel",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID id) {
        var cancelledOrder = orderService.cancelOrder(id, user.getId());
        return ResponseEntity.ok(orderMapper.toOrderResponseDTO(cancelledOrder));
    }
}
