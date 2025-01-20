-- Script to insert dummy dev data into the database.
USE ocommerce;
-- You first need to register two users into the system before running this scirpt.

-- Replace the id here with the first user id you want to have ownership of the orders.
SET @userId1 := 2;
-- Replace the id here with the second user id you want to have ownership of the orders.
SET @userId2 := 3;

DELETE FROM orderitems;
DELETE FROM orders;
DELETE FROM inventory;
DELETE FROM product;
DELETE FROM address;

INSERT INTO product (name, short_description, long_description, price,sku) VALUES ('Product #1', 'Product one short description.', 'This is a very long description of product #1.', 5.50,'12345');
INSERT INTO product (name, short_description, long_description, price,sku) VALUES ('Product #2', 'Product two short description.', 'This is a very long description of product #2.', 10.56,'23456');
INSERT INTO product (name, short_description, long_description, price,sku) VALUES ('Product #3', 'Product three short description.', 'This is a very long description of product #3.', 2.74,'34567');
INSERT INTO product (name, short_description, long_description, price,sku) VALUES ('Product #4', 'Product four short description.', 'This is a very long description of product #4.', 15.69,'45678');
INSERT INTO product (name, short_description, long_description, price,sku) VALUES ('Product #5', 'Product five short description.', 'This is a very long description of product #5.', 42.59,'56789');


SELECT product_id INTO @product1 FROM product WHERE name = 'Product #1';
SELECT product_id INTO @product2 FROM product WHERE name = 'Product #2';
SELECT product_id INTO @product3 FROM product WHERE name = 'Product #3';
SELECT product_id INTO @product4 FROM product WHERE name = 'Product #4';
SELECT product_id INTO @product5 FROM product WHERE name = 'Product #5';

INSERT INTO inventory (product_id, quantity) VALUES (@product1, 5);
INSERT INTO inventory (product_id, quantity) VALUES (@product2, 8);
INSERT INTO inventory (product_id, quantity) VALUES (@product3, 12);
INSERT INTO inventory (product_id, quantity) VALUES (@product4, 73);
INSERT INTO inventory (product_id, quantity) VALUES (@product5, 2);

INSERT INTO address (addressline1, city, country, zipcode, user_id) VALUES ('123 Tester Hill', 'Testerton', 'England', 30067, @userId1);
INSERT INTO address (addressline1, city, country, zipcode, user_id) VALUES ('312 Spring Boot', 'Hibernate', 'England', 50082, @userId2);

SELECT address_id INTO @address1 FROM address WHERE user_id = @userId1 ORDER BY address_id DESC;
SELECT address_id INTO @address2 FROM address WHERE user_id = @userId2 ORDER BY address_id DESC;

INSERT INTO orders (billingaddress_id, shippingaddress_id,user_id) VALUES (@address1, @address1, @userId1);
INSERT INTO orders (billingaddress_id, shippingaddress_id, user_id) VALUES (@address1, @address1, @userId1);
INSERT INTO orders (billingaddress_id, shippingaddress_id, user_id) VALUES (@address1, @address1, @userId1);
INSERT INTO orders (billingaddress_id, shippingaddress_id, user_id) VALUES (@address2, @address2, @userId2);
INSERT INTO orders (billingaddress_id, shippingaddress_id, user_id) VALUES (@address2, @address2, @userId2);


SELECT order_id INTO @order1 FROM orders WHERE billingaddress_id = @address1 AND user_id = @userId1 ORDER BY order_id DESC LIMIT 1;
SELECT order_id INTO @order2 FROM orders WHERE billingaddress_id = @address1 AND user_id = @userId1 ORDER BY order_id DESC LIMIT 1;
SELECT order_id INTO @order3 FROM orders WHERE billingaddress_id = @address1 AND user_id = @userId1 ORDER BY order_id DESC LIMIT 1;
SELECT order_id INTO @order4 FROM orders WHERE billingaddress_id = @address2 AND user_id = @userId2 ORDER BY order_id DESC LIMIT 1;
SELECT order_id INTO @order5 FROM orders WHERE billingaddress_id = @address2 AND user_id = @userId2 ORDER BY order_id DESC  LIMIT 1;

INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order1, @product1, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order1, @product2, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order2, @product3, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order2, @product2, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order2, @product5, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order3, @product3, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order4, @product4, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order4, @product2, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order5, @product3, 5);
INSERT INTO orderitems (order_id, product_id, quantity) VALUES (@order5, @product1, 5);