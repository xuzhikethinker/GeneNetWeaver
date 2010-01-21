function TestFunction(n)
% n is the dimension of the system

X = load('integration.tsv');
t = X(:,1);

% plot all the trajectories
for i=1:n
   X1 = X(:,i+1);
   plot(t,X1);
   hold on;
end

% plot the exact solution
s = 2/3 *exp(-3*t) + 1/3;
plot(t,s, 'r');
axis([0 1 0 1.2])

end