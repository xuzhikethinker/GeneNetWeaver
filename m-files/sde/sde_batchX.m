function sde_batch1X(n)

X = load('solutions.txt');
t = X(:,1);

for i=2:n
   X1 = X(:,i+1);
   plot(t,X1);
   hold on;
end


s = 2/3 *exp(-3*t) + 1/3;
plot(t,s, 'Color', [1 0.4 0], 'LineWidth', 1.2);

plot(t,X(:,2), 'r', 'LineWidth', 1.2);


% mu = 1.0;
% sigma = 0.6;
% 
% E = 1*exp(mu*t);
% 
% 
% plot(t,X)
% % semilogy(t,X)
% % hold on;
% % semilogy(t,E)



end